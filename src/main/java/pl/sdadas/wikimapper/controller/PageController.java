package pl.sdadas.wikimapper.controller;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.sdadas.wikimapper.model.Page;
import pl.sdadas.wikimapper.model.PagesStats;
import pl.sdadas.wikimapper.model.api.*;
import pl.sdadas.wikimapper.service.PageService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sławomir Dadas <sdadas@opi.org.pl>
 */
@CrossOrigin
@RestController
public class PageController {

    private final PageService service;

    @Autowired
    public PageController(PageService service) {
        this.service = service;
    }

    @PostMapping(path = "/pages", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Node> pages(@RequestBody SearchRequest request) {
        return this.nodes(service.search(request), request.getExpand());
    }

    @PostMapping(path = "/expand", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<Node> expand(@RequestBody ExpandRequest request) {
        return this.nodes(service.expand(request), request.getExpand());
    }

    @PostMapping(path = "/label", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void label(@RequestBody LabelRequest request) {
        this.service.setLabel(request.getIds(), request.getLabel());
    }

    @PostMapping(path = "/refresh")
    public void refresh() {
        this.service.refreshStats();
    }

    @GetMapping(path = "/stats")
    public PagesStats stats() {
        return this.service.getStats();
    }

    @GetMapping(path = "/download")
    public ResponseEntity<Resource> download() {
        Resource resource = service.getMappingFile();
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mapping.csv\"")
                .body(resource);
    }

    @ResponseBody
    @GetMapping(path = "/labels", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String labels() {
        return service.getLabelsJson();
    }

    private List<Node> nodes(Collection<? extends Page> values, ExpandType expand) {
        ExpandType expandType = ObjectUtils.firstNonNull(expand, ExpandType.CHILDREN);
        return values.stream().map(val -> new Node(val, expandType)).collect(Collectors.toList());
    }
}
