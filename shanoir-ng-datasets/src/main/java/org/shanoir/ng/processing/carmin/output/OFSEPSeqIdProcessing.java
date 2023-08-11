package org.shanoir.ng.processing.carmin.output;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.shanoir.ng.dataset.model.Dataset;
import org.shanoir.ng.dataset.service.DatasetService;
import org.shanoir.ng.datasetacquisition.model.DatasetAcquisition;
import org.shanoir.ng.datasetacquisition.model.mr.MrDatasetAcquisition;
import org.shanoir.ng.download.WADODownloaderService;
import org.shanoir.ng.processing.carmin.model.CarminDatasetProcessing;
import org.shanoir.ng.property.model.DatasetProperty;
import org.shanoir.ng.property.service.DatasetPropertyService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.PacsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class OFSEPSeqIdProcessing extends OutputProcessing {

    private static final Logger LOG = LoggerFactory.getLogger(OFSEPSeqIdProcessing.class);

    public static final String PIPELINE_OUTPUT = "output.json";

    private static final String[] SERIE_PROPERTIES = {
            "coil",
            "type",
            "protocolValidityStatus",
            "name",
            "numberOfDirections"
    };

    private static final String[] VOLUME_PROPERTIES = {
            "acquisitionDate",
            "contrastAgent",
            "contrastAgentAlgo",
            "contrastAgentAlgoConfidence",
            "contrastAgentDICOM",
            "organ",
            "organAlgo",
            "organAlgoConfidence",
            "organDICOM",
            "type",
            "sequence",
            "extraType",
            "derivedSequence",
            "name",
            "contrast",
            "bValue",
            "sliceThickness",
            "spacings",
            "spacingBetweenSlices",
            "numberOfSlices",
            "dimension",
            "dimensions",
            "axis"

    };

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private WADODownloaderService wadoDownloaderService;

    @Autowired
    private DatasetPropertyService datasetPropertyService;



    @Override
    public boolean canProcess(CarminDatasetProcessing processing) {
        return processing.getPipelineIdentifier().startsWith("ofsep_sequences_identification");
    }

    @Override
    public void manageTarGzResult(File resultFile, File parentFolder, CarminDatasetProcessing processing) throws OutputProcessingException {

        try (TarArchiveInputStream fin = new TarArchiveInputStream(
                new GzipCompressorInputStream(new FileInputStream(resultFile)))) {
            TarArchiveEntry entry;

            while ((entry = fin.getNextTarEntry()) != null) {

                String parsedEntry = entry.getName();

                if (entry.isDirectory()) {
                    continue;
                }

                if (parsedEntry.endsWith(PIPELINE_OUTPUT)) {

                    File cacheFolder = new File(parentFolder.getAbsolutePath() + File.separator + "cache");

                    if (!cacheFolder.exists()) {
                        cacheFolder.mkdirs();
                    }

                    File jsonFile = new File(cacheFolder, Paths.get(parsedEntry).getFileName().toString());
                    IOUtils.copy(fin, Files.newOutputStream(jsonFile.toPath()));

                    try (InputStream is = new FileInputStream(jsonFile)) {
                        JSONObject json = new JSONObject(IOUtils.toString(is, StandardCharsets.UTF_8));
                        this.processSeries(json.getJSONArray("series"), processing);
                    }

                    return;

                }
            }

        } catch (Exception e) {
            throw new OutputProcessingException("An error occured while extracting result from result archive.", e);
        }

    }

    private void processSeries(JSONArray series, CarminDatasetProcessing processing) throws JSONException, PacsException, EntityNotFoundException {
        for (int i = 0; i < series.length(); i++) {

            JSONObject serie = series.getJSONObject(i);
            Long serieId = serie.getLong("id");
            List<Dataset> datasets = datasetService.findByAcquisition(serieId);

            for(Dataset ds : datasets){

                long dsId = ds.getId();

                JSONObject vol;
                vol = this.getMatchingVolume(ds, serie);

                if(vol == null){
                    LOG.error("No volume from serie [{}] could be match with dataset [{}].", serieId, dsId);
                    continue;
                }

                try {
                    this.updateDataset(serie, ds, vol);
                } catch (EntityNotFoundException e) {
                    LOG.error("Error while updating dataset [{}]", dsId, e);
                    throw  e;
                }

                List<DatasetProperty> properties = this.getDatasetPropertiesFromVolume(ds, vol, processing);
                datasetPropertyService.create(properties);

            }
        }
    }

    /**
     * Update dataset from pipeline output
     *
     * @param serie
     * @param ds
     * @param vol
     * @throws JSONException
     * @throws EntityNotFoundException
     */
    private void updateDataset(JSONObject serie, Dataset ds, JSONObject vol) throws JSONException, EntityNotFoundException, org.shanoir.ng.shared.exception.EntityNotFoundException {
        DatasetAcquisition acq = ds.getDatasetAcquisition();

        if(acq instanceof MrDatasetAcquisition){
            ((MrDatasetAcquisition) acq).getMrProtocol()
                    .getOriginMetadata()
                    .setMrSequenceName(serie.getString("type"));
        }

        ds.getOriginMetadata().setName(vol.getString("type"));

        datasetService.update(ds);
    }


    /**
     * Create dataset properties from pipeline output
     *
     * @param ds
     * @param volume
     * @return
     */
    private List<DatasetProperty> getDatasetPropertiesFromVolume(Dataset ds, JSONObject volume, CarminDatasetProcessing processing) throws JSONException {
        List<DatasetProperty> properties = new ArrayList<>();

        for(String name : SERIE_PROPERTIES){

            if(!volume.has(name)){
                continue;
            }

            DatasetProperty property = new DatasetProperty();
            property.setDataset(ds);
            property.setName("serie." + name);
            property.setValue(volume.getString(name));
            property.setProcessing(processing);
            properties.add(property);
        }

        for(String name : VOLUME_PROPERTIES){

            if(!volume.has(name)){
                continue;
            }

            DatasetProperty property = new DatasetProperty();
            property.setDataset(ds);
            property.setName("volume." + name);
            property.setValue(volume.getString(name));
            property.setProcessing(processing);
            properties.add(property);
        }

        return properties;

    }


    /**
     * Get JSON volume matching Shanoir dataset
     * Match is made by orientation DICOM property
     *
     * @param dataset
     * @return
     * @throws JSONException
     */
    public JSONObject getMatchingVolume(Dataset dataset, JSONObject serie) throws JSONException, PacsException {

        JSONArray volumes = serie.getJSONArray("volumes");

        Attributes attributes = wadoDownloaderService.getDicomAttributesForDataset(dataset);

        double[] dsOrientation = attributes.getDoubles(Tag.ImageOrientationPatient);

        for (int i = 0 ; i < volumes.length(); i++) {

            JSONObject volume = volumes.getJSONObject(i);
            JSONArray volOrientation = volume.getJSONArray("orientation");

            if(this.areOrientationsEquals(dsOrientation, volOrientation)){
                return volume;
            }

        }

        return null;

    }

    public boolean areOrientationsEquals(double[] dsOrientation, JSONArray volOrientation) throws JSONException {

        if(dsOrientation.length != volOrientation.length()){
            return false;
        }

        for (int i = 0 ; i < dsOrientation.length; i++) {
            if(dsOrientation[i] != volOrientation.getDouble(i)){
                return false;
            }
        }

        return true;

    }

}