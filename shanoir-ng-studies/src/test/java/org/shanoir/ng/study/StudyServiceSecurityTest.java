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

package org.shanoir.ng.study;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessAuthorized;
import static org.shanoir.ng.utils.assertion.AssertUtils.assertAccessDenied;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.study.repository.StudyRepository;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.utils.ModelsUtil;
import org.shanoir.ng.utils.usermock.WithMockKeycloakUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;

/**
 * User security service test.
 * 
 * @author jlouis
 * 
 */

@SpringBootTest
@ActiveProfiles("test")
public class StudyServiceSecurityTest {

	private static final long LOGGED_USER_ID = 2L;
	private static final String LOGGED_USER_USERNAME = "logged";
	private static final long ENTITY_ID = 1L;
	
	private Study mockNew;
	private Study mockExisting;
	
	@Autowired
	private StudyService service;
	
	@MockBean
	private StudyRepository repository;
	
	@BeforeEach
	public void setup() {
		mockNew = ModelsUtil.createStudy();
		mockExisting = ModelsUtil.createStudy();
		mockExisting.setId(ENTITY_ID);
	}
	
	@Test
	@WithAnonymousUser
	public void testAsAnonymous() throws ShanoirException {
		assertAccessDenied(service::findById, ENTITY_ID);
		assertAccessDenied(service::findAll);
		assertAccessDenied(service::create, mockNew);
		assertAccessDenied(service::update, mockExisting);
		assertAccessDenied(service::deleteById, ENTITY_ID);
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testAsUser() throws ShanoirException {
		assertAccessDenied(service::create, mockNew);
		assertAccessDenied(service::update, mockExisting);
		assertAccessDenied(service::deleteById, ENTITY_ID);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testFindByIdAsUserThatCanSee() throws ShanoirException {
		
		given(repository.findById(1L)).willReturn(Optional.of(buildStudyMock(1L)));
		assertAccessDenied(service::findById, 1L);
		
		given(repository.findById(1L)).willReturn(Optional.of(buildStudyMock(1L, StudyUserRight.CAN_ADMINISTRATE, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT)));
		assertAccessAuthorized(service::findById, 1L);
		
		given(repository.findById(1L)).willReturn(Optional.of(buildStudyMock(1L, StudyUserRight.CAN_SEE_ALL)));
		assertAccessAuthorized(service::findById, 1L);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void testFindAllAsUserThatCanSee() throws ShanoirException {
		assertAccessAuthorized(service::findAll);
		
		given(repository.findByStudyUserList_UserIdAndStudyUserList_StudyUserRightsAndStudyUserList_Confirmed_OrderByNameAsc(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId(), true)).willReturn(Arrays.asList(new Study[] 
				{ buildStudyMock(1L, StudyUserRight.CAN_SEE_ALL), buildStudyMock(2L, StudyUserRight.CAN_SEE_ALL) } ));
		assertEquals(2, service.findAll().size());
		
		given(repository.findByStudyUserList_UserIdAndStudyUserList_StudyUserRightsAndStudyUserList_Confirmed_OrderByNameAsc(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId(), true)).willReturn(Arrays.asList(new Study[] 
				{ buildStudyMock(1L, StudyUserRight.CAN_SEE_ALL), buildStudyMock(2L, StudyUserRight.CAN_DOWNLOAD) } ));
		assertEquals(1, service.findAll().size());
	}

	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_EXPERT" })
	public void testAsExpert() throws ShanoirException {
		given(repository.findById(ENTITY_ID)).willReturn(Optional.of(buildStudyMock(ENTITY_ID, StudyUserRight.CAN_ADMINISTRATE)));
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findAll);
		assertAccessAuthorized(service::create, mockNew);
		assertAccessAuthorized(service::create, mockExisting);
		
		Study mockOne = buildStudyMock(1L, StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_DOWNLOAD, StudyUserRight.CAN_IMPORT);
		given(repository.findById(1L)).willReturn(Optional.of(mockOne));
		assertAccessDenied(service::update, mockOne);
		assertAccessDenied(service::deleteById, 1L);
		
		Study mockTwo = buildStudyMock(2L, StudyUserRight.CAN_ADMINISTRATE);
		given(repository.findById(2L)).willReturn(Optional.of(mockTwo));
		assertAccessAuthorized(service::update, mockTwo);
		assertAccessAuthorized(service::deleteById, 2L);
		
		Study mockThree = buildStudyMock(3L);
		given(repository.findById(3L)).willReturn(Optional.of(mockThree));
		assertAccessDenied(service::update, mockThree);
		assertAccessDenied(service::deleteById, 3L);
		
		
		Study study = service.findById(1L);
		assertTrue(study != null && study.getStudyUserList() != null && study.getStudyUserList().size() > 0);
		Study hackedStudy = new Study(); hackedStudy.setId(3L);
		study.getStudyUserList().get(0).setStudy(hackedStudy);
		assertAccessDenied(service::update, study);
	}	
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void testAsAdmin() throws ShanoirException {
		given(repository.findById(ENTITY_ID)).willReturn(Optional.of(buildStudyMock(ENTITY_ID)));
		assertAccessAuthorized(service::findById, ENTITY_ID);
		assertAccessAuthorized(service::findAll);
		assertAccessAuthorized(service::create, mockNew);
		assertAccessAuthorized(service::create, mockExisting);
		assertAccessAuthorized(service::update, mockExisting);
		assertAccessAuthorized(service::deleteById, ENTITY_ID);
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_ADMIN" })
	public void findAllTestAdmin() {
		given(repository.findAll()).willReturn(Arrays.asList(ModelsUtil.createStudy()));
		List<Study> all = service.findAll();
		List<Study> repoAll = repository.findAll();
		Assertions.assertNotNull(all);
		Assertions.assertEquals(repoAll.size(), all.size());
		Assertions.assertTrue(all.size() > 0);
		Assertions.assertNotNull(all.get(0).getStudyCenterList());
	}
	
	@Test
	@WithMockKeycloakUser(id = LOGGED_USER_ID, username = LOGGED_USER_USERNAME, authorities = { "ROLE_USER" })
	public void findAllTestUser() {
		Study studyMock = ModelsUtil.createStudy();
		StudyUser studyUser = new StudyUser();
		studyUser.setStudy(studyMock); studyUser.setUserId(LOGGED_USER_ID); studyUser.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_SEE_ALL));
		studyMock.setStudyUserList(Arrays.asList(studyUser));
		given(repository.findByStudyUserList_UserIdAndStudyUserList_StudyUserRightsAndStudyUserList_Confirmed_OrderByNameAsc(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId(), true))
			.willReturn(Arrays.asList(studyMock));
		List<Study> repoAll = repository.findByStudyUserList_UserIdAndStudyUserList_StudyUserRightsAndStudyUserList_Confirmed_OrderByNameAsc(LOGGED_USER_ID, StudyUserRight.CAN_SEE_ALL.getId(), true);
		List<Study> all = service.findAll();
		Assertions.assertNotNull(all);
		Assertions.assertEquals(repoAll.size(), all.size());
		Assertions.assertTrue(repoAll.size() > 0);
		Assertions.assertTrue(all.size() > 0);
		Assertions.assertNotNull(all.get(0).getStudyCenterList());
	}
	
	private Study buildStudyMock(Long id, StudyUserRight... rights) {
		Study study = ModelsUtil.createStudy();
		study.setId(id);
		List<StudyUser> studyUserList = new ArrayList<>();
		StudyUser studyUser = new StudyUser();
		studyUser.setUserId(LOGGED_USER_ID);
		studyUser.setUserName(LOGGED_USER_USERNAME);
		studyUser.setStudy(study);
		studyUser.setStudyUserRights(Arrays.asList(rights));
		studyUserList.add(studyUser);			
		study.setStudyUserList(studyUserList);
		return study;		
	}

}
