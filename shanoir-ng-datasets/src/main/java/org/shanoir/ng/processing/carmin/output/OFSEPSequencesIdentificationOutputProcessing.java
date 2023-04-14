package org.shanoir.ng.processing.carmin.output;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
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
import org.shanoir.ng.processing.model.DatasetProcessing;
import org.shanoir.ng.property.model.DatasetProperty;
import org.shanoir.ng.property.service.DatasetPropertyService;
import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class OFSEPSequencesIdentificationOutputProcessing extends OutputProcessing {

    public static final String OUTPUT = "output.json";

    private static final Logger LOG = LoggerFactory.getLogger(OFSEPSequencesIdentificationOutputProcessing.class);

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
    public boolean doManage(String pipelineIdentifier) {
        return pipelineIdentifier.equals("ofsep_sequences_identification/0.1");
    }

    @Override
    public void manageTarGzResult(File resultFile, File parentFolder, CarminDatasetProcessing processing) {
        JSONArray series;
        try {
            series = this.getJsonFromInput(resultFile);
        } catch (JSONException | IOException e) {
            LOG.error("Could not read JSON file [{}]", OUTPUT, e);
            return;
        }

        if(series == null){
            LOG.error("File [{}] not found.", OUTPUT);
            return;
        }

        try {
            this.processSeries(series, processing);
        } catch (JSONException e) {
            LOG.error("Error while parsing pipeline output", e);
        }
    }

    private void processSeries(JSONArray series, CarminDatasetProcessing processing) throws JSONException {
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
                    return;
                }

                this.createDatasetProperties(ds, vol, processing);
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
    private void updateDataset(JSONObject serie, Dataset ds, JSONObject vol) throws JSONException, EntityNotFoundException {
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
    private void createDatasetProperties(Dataset ds, JSONObject volume, CarminDatasetProcessing processing) throws JSONException {
        List<DatasetProperty> properties = new ArrayList<>();

        for(String name : SERIE_PROPERTIES){

            DatasetProperty property = new DatasetProperty();
            property.setDataset(ds);
            property.setName("serie." + name);
            property.setValue(volume.getString(name));
            property.setProcessing(processing);
            properties.add(property);
        }

        for(String name : VOLUME_PROPERTIES){

            DatasetProperty property = new DatasetProperty();
            property.setDataset(ds);
            property.setName("volume." + name);
            property.setValue(volume.getString(name));
            property.setProcessing(processing);
            properties.add(property);
        }

        datasetPropertyService.create(properties);

    }


    /**
     * Get JSON volume matching Shanoir dataset
     * Match is made by orientation DICOM property
     *
     * @param dataset
     * @return
     * @throws JSONException
     */
    private JSONObject getMatchingVolume(Dataset dataset, JSONObject serie) throws JSONException {

        JSONArray volumes = serie.getJSONArray("volumes");

        Attributes attributes = wadoDownloaderService.getDicomAttributesForDataset(dataset);

        String orientation = attributes.getString(Tag.ImageOrientationPatient);

        for (int i = 0 ; i < volumes.length(); i++) {

            JSONObject volume;
            String volOrientation;

            volume = volumes.getJSONObject(i);
            volOrientation = volume.getString("orientation");

            if(orientation.equals(volOrientation)){
                return volume;
            }

        }

        return null;

    }

    /**
     * Get series JSONArray from pipeline output
     *
     * @param resultFile
     * @return
     * @throws JSONException
     * @throws IOException
     */
    private JSONArray getJsonFromInput(File resultFile) throws JSONException, IOException {
        try (TarArchiveInputStream fin = new TarArchiveInputStream(
                new GzipCompressorInputStream(new FileInputStream(resultFile)))) {

            TarArchiveEntry entry;

            while ((entry = fin.getNextTarEntry()) != null) {

                if (entry.isDirectory() ||
                        !entry.getName().endsWith(OUTPUT)) {
                    continue;
                }

                StringBuilder sb = new StringBuilder();
                JSONObject json = new JSONObject(sb.toString());
                return json.getJSONArray("series");

            }
        }
        return null;
    }

}
