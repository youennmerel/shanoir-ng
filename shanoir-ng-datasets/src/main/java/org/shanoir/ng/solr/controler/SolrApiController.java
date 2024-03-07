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

/**
 * NOTE: This class is auto generated by the swagger code generator program (2.2.3).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.shanoir.ng.solr.controler;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.apache.solr.client.solrj.SolrServerException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.solr.model.ShanoirSolrDocument;
import org.shanoir.ng.solr.model.ShanoirSolrQuery;
import org.shanoir.ng.solr.service.SolrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.SolrResultPage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;

/**
 * @author yyao
 *
 */
@Controller
public class SolrApiController implements SolrApi {
	
	@Autowired
	private SolrService solrService;
	
	@Override
	public ResponseEntity<Void> indexAll() throws RestServiceException, SolrServerException, IOException {
		solrService.indexAll();		
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<SolrResultPage<ShanoirSolrDocument>> facetSearch(
			@Parameter(description = "facets", required = true) @Valid @RequestBody ShanoirSolrQuery facet, Pageable pageable) throws RestServiceException {
		SolrResultPage<ShanoirSolrDocument> documents = solrService.facetSearch(facet, pageable);
		return new ResponseEntity<SolrResultPage<ShanoirSolrDocument>>(documents, HttpStatus.OK);
	}
	
	@Override
	public ResponseEntity<Page<ShanoirSolrDocument>> findByIdIn(@Parameter(description = "dataset ids", required = true) @Valid @RequestBody List<Long> datasetIds, Pageable pageable) throws RestServiceException {
		Page<ShanoirSolrDocument> documents = solrService.getByIdIn(datasetIds, pageable);
		if (documents.getContent().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<Page<ShanoirSolrDocument>>(documents, HttpStatus.OK);
	}
	
}