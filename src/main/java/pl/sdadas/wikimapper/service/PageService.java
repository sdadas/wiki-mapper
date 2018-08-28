package pl.sdadas.wikimapper.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import pl.sdadas.wikimapper.model.*;
import pl.sdadas.wikimapper.model.api.ExpandRequest;
import pl.sdadas.wikimapper.model.api.ExpandType;
import pl.sdadas.wikimapper.model.api.SearchRequest;
import pl.sdadas.wikimapper.model.api.ShowType;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Sławomir Dadas
 */
@Service
public class PageService implements ListenableFutureCallback<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(PageService.class);

    private Pages pages;

    private AsyncListenableTaskExecutor executor;

    private String labelsJson;

    private boolean refreshing = false;

    @Value("${data.path}")
    private String dataPath;

    @Autowired
    public PageService(AsyncListenableTaskExecutor executor) {
        this.executor = executor;
    }

    @PostConstruct
    private void init() {
        Validate.notNull(this.dataPath, "data.path property is null");
        this.pages = Pages.load(new File(dataPath));
        this.labelsJson = loadLabels();
        refreshStats();
    }

    private String loadLabels() {
        File labelsFile = new File(this.dataPath, "labels.json");
        try {
            return FileUtils.readFileToString(labelsFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @PreDestroy
    private void destroy() {
        this.pages.close();
    }

    public void setLabel(List<String> ids, String label) {
        if(ids == null) return;
        this.pages.setLabel(ids, label);
    }

    public PagesStats getStats() {
        PagesStats result = this.pages.getStats();
        result.setRefreshing(this.refreshing);
        return result;
    }

    public List<Page> search(SearchRequest request) {
        int limit = ObjectUtils.firstNonNull(request.getLimit(), 100);
        int offset = ObjectUtils.firstNonNull(request.getOffset(), 0);
        ShowType show = ObjectUtils.firstNonNull(request.getShow(), ShowType.ALL);
        ExpandType expand = ObjectUtils.firstNonNull(request.getExpand(), ExpandType.CHILDREN);

        FieldIndex idx = pages.index(request.getSort());
        List<? extends Page> objects = expand.equals(ExpandType.ARTICLE_PARENTS) ? idx.articles() : idx.categories();
        List<Page> res = new ArrayList<>();
        QueryType queryType = QueryType.queryType(request.getSearch());
        String query = StringUtils.strip(request.getSearch(), "^$");
        int found = 0;
        for (Page page: objects) {
            if(queryType.matches(query, page) && show.matches(page)) {
                found++;
                if(found >= offset) res.add(page);
            }
            if(res.size() >= limit) break;
        }
        return res;
    }

    public List<Page> expand(ExpandRequest request) {
        if(request.getExpand() == null || request.getNodeId() == null) return Collections.emptyList();
        Long nodeId = request.getNodeId();
        Page page = pages.getCategories().get(nodeId);
        if(page == null) page = pages.getArticles().get(nodeId);
        if(page == null) return Collections.emptyList();
        return request.getExpand().expand(page);
    }

    public void refreshStats() {
        if(this.refreshing) {
            LOG.warn("Tried to refresh while data is still refreshing");
            return;
        }
        ListenableFuture<?> future = executor.submitListenable(() -> pages.refreshStats());
        future.addCallback(this);
        this.refreshing = true;
    }

    public Resource getMappingFile() {
        StringBuilder builder = new StringBuilder();
        pages.getArticles().values().stream()
                .filter(val -> val.getAssignedLabel() != null)
                .sorted(Comparator.comparingLong(Page::getId))
                .forEach(val -> builder.append(String.format("%d,%s,%s\n",
                        val.getId(), val.getName(), val.getAssignedLabel())));
        return new ByteArrayResource(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    public String getLabelsJson() {
        return labelsJson;
    }

    @Override
    public void onFailure(Throwable ex) {
        ex.printStackTrace();
        this.refreshing = false;
    }

    @Override
    public void onSuccess(Object result) {
        this.refreshing = false;
    }
}
