/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.study.controler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.dto.IdNameCenterStudyDTO;
import org.shanoir.ng.study.dto.PublicStudyDTO;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.dua.DataUserAgreement;
import org.shanoir.ng.study.model.Study;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-03-23T10:35:29.288Z")

@Tag(name = "studies", description = "the studies API")
@RequestMapping("/studies")
public interface StudyApi {

	@ApiOperation(value = "", notes = "Deletes a study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "study deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{studyId}", produces = { "application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> deleteStudy(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@ApiOperation(value = "", notes = "If exists, returns the studies that the user is allowed to see", response = Study.class, tags = {})
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found studies"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.filterStudyDTOsHasRight(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<StudyDTO>> findStudies();

	@ApiOperation(value = "", notes = "If exists, returns the studies that are publicly available", response = Study.class, tags = {})
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found studies"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/public/data", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<PublicStudyDTO>> findPublicStudiesData();

	@ApiOperation(value = "", notes = "Returns id and name for all the studies", response = IdName.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found studies", response = IdName.class, responseContainer = "List"),
			@ApiResponse(responseCode = "204", description = "no study found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/names", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.filterStudyIdNameDTOsHasRight(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<IdName>> findStudiesNames() throws RestServiceException;

	@ApiOperation(value = "", notes = "Returns id, name and centers for all the studies", response = IdName.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found studies", response = IdName.class, responseContainer = "List"),
			@ApiResponse(responseCode = "204", description = "no study found"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/namesAndCenters", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("hasRole('ADMIN') or @studySecurityService.filterStudyIdNameDTOsHasRight(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<List<IdNameCenterStudyDTO>> findStudiesNamesAndCenters() throws RestServiceException;

	@ApiOperation(value = "", notes = "If exists, returns the study corresponding to the given id", response = Study.class, tags = {})
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found study"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	@PostAuthorize("@studySecurityService.hasRightOnTrustedStudyDTO(returnObject.getBody(), 'CAN_SEE_ALL')")
	ResponseEntity<StudyDTO> findStudyById(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@ApiOperation(value = "", notes = "Saves a new study", response = Study.class, tags = {})
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "created study"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT')")
	ResponseEntity<StudyDTO> saveNewStudy(
			@ApiParam(value = "study to create", required = true) @RequestBody Study study, BindingResult result)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "Updates a study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "study updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/{studyId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.PUT)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @controlerSecurityService.idMatches(#studyId, #study) and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> updateStudy(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "study to update", required = true) @RequestBody Study study, BindingResult result)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "Get my rights on this study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "here are your rights"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/rights/{studyId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<StudyUserRight>> rights(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId)
			throws RestServiceException;
	
	@ApiOperation(value = "", notes = "Get my rights", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "here are your rights"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/rights/all", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<Map<Long, List<StudyUserRight>>> rights() throws RestServiceException;

	@ApiOperation(value = "", notes = "Know if I'm in one study at least with CAN_IMPORT", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = ""),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/hasOneStudy", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<Boolean> hasOneStudyToImport() throws RestServiceException;

	@ApiOperation(value = "", notes = "Add protocol file to a study", response = Void.class, tags = {})
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "protocol file"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "protocol-file-upload/{studyId}", produces = { "application/json" }, consumes = {
			"multipart/form-data" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> uploadProtocolFile(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "file to upload", required = true) @Valid @RequestBody MultipartFile file)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "Download protocol file from a study", tags = {})
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "protocol file"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "protocol-file-download/{studyId}/{fileName:.+}/")
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_DOWNLOAD'))")
	void downloadProtocolFile(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "file to download", required = true) @PathVariable("fileName") String fileName, HttpServletResponse response) throws RestServiceException, IOException;
	
	@ApiOperation(value = "", notes = "If one or more exist, return a list of data user agreements (DUAs) waiting for the given user id", response = DataUserAgreement.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "found duas"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no duas found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/dua", produces = { "application/json" }, method = RequestMethod.GET)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<List<DataUserAgreement>> getDataUserAgreements()
			throws RestServiceException, IOException;
	
	@ApiOperation(value = "", notes = "Updates a data user agreement (DUA)", response = Void.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "dua updated"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PutMapping(value = "/dua/{duaId}", produces = { "application/json" }, consumes = {"application/json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER') and @studySecurityService.checkUserOnDUA(#duaId)")
	ResponseEntity<Void> acceptDataUserAgreement(
			@ApiParam(value = "id of the dua", required = true) @PathVariable("duaId") Long duaId)
			throws RestServiceException, MicroServiceCommunicationException;

	@ApiOperation(value = "", notes = "Add DUA to a study", response = Void.class, tags = {})
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "dua uploaded"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@PostMapping(value = "dua-upload/{studyId}", produces = { "application/json" }, consumes = {
			"multipart/form-data" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> uploadDataUserAgreement(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "file to upload", required = true) @Valid @RequestBody MultipartFile file)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "Download DUA of a study", tags = {})
	@ApiResponses(value = {
	        @ApiResponse(responseCode = "200", description = "dua downloaded"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "422", description = "bad parameters"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@GetMapping(value = "dua-download/{studyId}/{fileName:.+}/")
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	void downloadDataUserAgreement(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "file to download", required = true) @PathVariable("fileName") String fileName, HttpServletResponse response) throws RestServiceException, IOException;

	@ApiOperation(value = "", notes = "Deletes the DUA of a study", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "dua deleted"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "dua-delete/{studyId}", produces = {
			"application/json" }, method = RequestMethod.DELETE)
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT') and @studySecurityService.hasRightOnStudy(#studyId, 'CAN_ADMINISTRATE')")
	ResponseEntity<Void> deleteDataUserAgreement(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId)
			throws IOException;

	@ApiOperation(value = "", notes = "If exists, returns the studies that are publicly available for a given user", response = Study.class, tags = {})
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "found studies"),
			@ApiResponse(responseCode = "401", description = "unauthorized"),
			@ApiResponse(responseCode = "403", description = "forbidden"),
			@ApiResponse(responseCode = "404", description = "no study found"),
			@ApiResponse(responseCode = "500", description = "unexpected error") })
	@RequestMapping(value = "/public/connected", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<List<IdName>> findPublicStudiesConnected();

}