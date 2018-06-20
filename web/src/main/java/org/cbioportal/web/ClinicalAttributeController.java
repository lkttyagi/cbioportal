package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.Direction;
import org.cbioportal.web.parameter.HeaderKeyConstants;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.ClinicalAttributeFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.sort.ClinicalAttributeSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@PublicApi
@RestController
@Validated
@Api(tags = "Clinical Attributes", description = " ")
public class ClinicalAttributeController {

    @Autowired
    private ClinicalAttributeService clinicalAttributeService;

    @RequestMapping(value = "/clinical-attributes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all clinical attributes")
    public ResponseEntity<List<ClinicalAttribute>> getAllClinicalAttributes(
            @ApiParam("Level of detail of the response")
            @RequestParam(defaultValue = "SUMMARY") Projection projection,
            @ApiParam("Page size of the result list")
            @Max(PagingConstants.MAX_PAGE_SIZE)
            @Min(PagingConstants.MIN_PAGE_SIZE)
            @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
            @ApiParam("Page number of the result list")
            @Min(PagingConstants.MIN_PAGE_NUMBER)
            @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
            @ApiParam("Name of the property that the result list is sorted by")
            @RequestParam(required = false) ClinicalAttributeSortBy sortBy,
            @ApiParam("Direction of the sort")
            @RequestParam(defaultValue = "ASC") Direction direction) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, clinicalAttributeService.getMetaClinicalAttributes()
                    .getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    clinicalAttributeService.getAllClinicalAttributes(projection.name(), pageSize, pageNumber,
                            sortBy == null ? null : sortBy.getOriginalValue(), direction.name()), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    @RequestMapping(value = "/studies/{studyId}/clinical-attributes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all clinical attributes in the specified study")
    public ResponseEntity<List<ClinicalAttribute>> getAllClinicalAttributesInStudy(
            @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
            @PathVariable String studyId,
            @ApiParam("Level of detail of the response")
            @RequestParam(defaultValue = "SUMMARY") Projection projection,
            @ApiParam("Page size of the result list")
            @Max(PagingConstants.MAX_PAGE_SIZE)
            @Min(PagingConstants.MIN_PAGE_SIZE)
            @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
            @ApiParam("Page number of the result list")
            @Min(PagingConstants.MIN_PAGE_NUMBER)
            @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
            @ApiParam("Name of the property that the result list is sorted by")
            @RequestParam(required = false) ClinicalAttributeSortBy sortBy,
            @ApiParam("Direction of the sort")
            @RequestParam(defaultValue = "ASC") Direction direction) throws StudyNotFoundException {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, clinicalAttributeService
                    .getMetaClinicalAttributesInStudy(studyId).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    clinicalAttributeService.getAllClinicalAttributesInStudy(studyId, projection.name(), pageSize,
                            pageNumber, sortBy == null ? null : sortBy.getOriginalValue(), direction.name()),
                    HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    @RequestMapping(value = "/studies/{studyId}/clinical-attributes/{clinicalAttributeId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get specified clinical attribute")
    public ResponseEntity<ClinicalAttribute> getClinicalAttributeInStudy(
            @ApiParam(required = true, value = "Study ID e.g. acc_tcga")
            @PathVariable String studyId,
            @ApiParam(required = true, value= "Clinical Attribute ID e.g. CANCER_TYPE")
            @PathVariable String clinicalAttributeId) 
        throws ClinicalAttributeNotFoundException, StudyNotFoundException {

        return new ResponseEntity<>(clinicalAttributeService.getClinicalAttribute(studyId, clinicalAttributeId),
                HttpStatus.OK);
    }

    @PreAuthorize("hasPermission(#studyIds, 'List<CancerStudyId>', 'read')")
    @RequestMapping(value = "/clinical-attributes/fetch", method = RequestMethod.POST, 
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch clinical attributes")
    public ResponseEntity<List<ClinicalAttribute>> fetchClinicalAttributes(
        @ApiParam(required = true, value = "List of Study IDs")
        @Size(min = 1, max = PagingConstants.MAX_PAGE_SIZE)
        @RequestBody List<String> studyIds,
        @ApiParam("Level of detail of the response")
        @RequestParam(defaultValue = "SUMMARY") Projection projection) {

        if (projection == Projection.META) {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, clinicalAttributeService.fetchMetaClinicalAttributes(
                studyIds).getTotalCount().toString());
            return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(clinicalAttributeService.fetchClinicalAttributes(studyIds, projection.name()), 
                HttpStatus.OK);
        }
    }

    @PreAuthorize("hasPermission(#clinicalAttributeFilter, 'ClinicalAttributeFilter', 'read')")
    @RequestMapping(value = "/clinical-attributes/counts/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get all clinical attributes in specified sampleIdentifiers or sampleListID with clinical attribute count")
    public ResponseEntity<List<ClinicalAttribute>> getAllClinicalAttributesInStudies(
            @ApiParam(required = true, value = "List of SampleIdentifiers or Sample List ID")
            @Valid @RequestBody ClinicalAttributeFilter clinicalAttributeFilter,
            @RequestParam(defaultValue = "SUMMARY") Projection projection,
            @ApiParam("Name of the property that the result list is sorted by")
            @RequestParam(required = false) ClinicalAttributeSortBy sortBy,
            @ApiParam("Direction of the sort")
            @RequestParam(defaultValue = "ASC") Direction direction) {

        List<ClinicalAttribute> clinicalAttributeList;
        if (clinicalAttributeFilter.getSampleListId() != null) {
            clinicalAttributeList = clinicalAttributeService.getAllClinicalAttributesInStudiesBySampleListId(
                    clinicalAttributeFilter.getSampleListId(), projection.name(),
                    sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
        } else {
            List<SampleIdentifier> sampleIdentifiers = clinicalAttributeFilter.getSampleIdentifiers();
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
                studyIds.add(sampleIdentifier.getStudyId());
                sampleIds.add(sampleIdentifier.getSampleId());
            }
            clinicalAttributeList = clinicalAttributeService.getAllClinicalAttributesInStudiesBySampleIds(studyIds,
                    sampleIds, projection.name(), sortBy == null ? null : sortBy.getOriginalValue(), direction.name());
        }

        return new ResponseEntity<>(clinicalAttributeList, HttpStatus.OK);
    }
}
