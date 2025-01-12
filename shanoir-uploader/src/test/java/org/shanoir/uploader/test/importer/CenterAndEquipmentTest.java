package org.shanoir.uploader.test.importer;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Center;
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.Manufacturer;
import org.shanoir.uploader.model.rest.ManufacturerModel;
import org.shanoir.uploader.test.AbstractTest;

public class CenterAndEquipmentTest extends AbstractTest {

	private static Logger logger = Logger.getLogger(CenterAndEquipmentTest.class);
	
	@Test
	public void createCenterTest() throws Exception {
		Center createdCenter = createCenter();
		Assertions.assertNotNull(createdCenter);
	}
	
	@Test
	public void createEquipmentAndFindBySerialNumber() throws Exception {
		Center createdCenter = createCenter();
		Manufacturer manufacturer = new Manufacturer();
		manufacturer.setName("Manufacturer-" + UUID.randomUUID().toString());
		Manufacturer createdManufacturer = shUpClient.createManufacturer(manufacturer);
		Assertions.assertNotNull(createdManufacturer);
		ManufacturerModel manufacturerModel = new ManufacturerModel();
		manufacturerModel.setName("Manufacturer-Model-" + UUID.randomUUID().toString());
		manufacturerModel.setManufacturer(createdManufacturer);
		manufacturerModel.setDatasetModalityType("0"); // 0 == MR
		manufacturerModel.setMagneticField(3.0);
		ManufacturerModel createdManufacturerModel = shUpClient.createManufacturerModel(manufacturerModel);
		Assertions.assertNotNull(createdManufacturerModel);
		AcquisitionEquipment equipment = new AcquisitionEquipment();
		String serialNumberRandom = "Serial-Number-" + UUID.randomUUID().toString();
		equipment.setSerialNumber(serialNumberRandom);
		equipment.setCenter(new IdName(createdCenter.getId(), createdCenter.getName()));
		equipment.setManufacturerModel(createdManufacturerModel);
		AcquisitionEquipment createdEquipment = shUpClient.createEquipment(equipment);
		Assertions.assertNotNull(createdEquipment);
		List<AcquisitionEquipment> equipments = shUpClient.findAcquisitionEquipmentsBySerialNumber(serialNumberRandom);
		Assertions.assertNotNull(equipments);
	}
	
}
