package pl.sdadas.wikimapper.model;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Sławomir Dadas
 */
public class Pages implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(Pages.class);

    private Map<Long, Page> articles = new HashMap<>();

    private Map<Long, Category> categories = new HashMap<>();

    private Map<String, FieldIndex> indexes;

    private PagesStats stats;

    private File dataDir;

    private File mappingLogFile;

    private PrintWriter mappingLogWriter;

    public static Pages load(File dataDir) {
        try {
            return new Pages(dataDir);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Pages(File dataDir) throws IOException {
        this.dataDir = dataDir;
        this.mappingLogFile = new File(dataDir, "mappingLog.csv");
        loadCollectionsData();
        this.stats = new PagesStats(this.articles.size());
    }

    private void loadCollectionsData() throws IOException {
        if(this.articles.size() > 0 && this.categories.size() > 0) return;
        loadPages();
        loadParents("articleParents.csv", id -> this.articles.get(id));
        loadParents("categoryParents.csv", id -> this.categories.get(id));
        loadMappingLog();
        loadIndexes();
        this.mappingLogWriter = new PrintWriter(new FileWriter(this.mappingLogFile, true));
    }

    private void loadIndexes() {
        Map<String, FieldIndex> results = new HashMap<>();
        results.put("id", createIndex(Comparator.comparingLong(Page::getId)));
        results.put("articles", createIndex((o1, o2) -> Integer.compare(o2.getChildArticles(), o1.getChildArticles())));
        results.put("labelled", createIndex((o1, o2) -> Integer.compare(o2.getLabelledArticles(), o1.getLabelledArticles())));
        results.put("unlabelled", createIndex((o1, o2) -> Integer.compare(o2.getUnlabelledArticles(), o1.getUnlabelledArticles())));
        results.put("lpercent", createIndex((o1, o2) -> new CompareToBuilder()
                    .append(o2.getLabelledPercent(), o1.getLabelledPercent())
                    .append(o2.getChildArticles(), o1.getChildArticles())
                    .toComparison()
        ));
        results.put("upercent", createIndex((o1, o2) -> new CompareToBuilder()
                .append(o1.getLabelledPercent(), o2.getLabelledPercent())
                .append(o2.getChildArticles(), o1.getChildArticles())
                .toComparison()
        ));
        this.indexes = results;
    }

    private FieldIndex createIndex(Comparator<Page> comparator) {
        return new FieldIndex(this.articles.values(), this.categories.values(), comparator);
    }

    public synchronized void setLabel(List<String> ids, String label) {
        for (String id: ids) {
            if(StringUtils.isNumeric(id)) {
                Long longId = Long.valueOf(id);
                Page page = categories.get(longId);
                if(page == null) page = articles.get(longId);
                Validate.notNull(page, "page is null");
                page.setLabel(label);
                mappingLogWriter.write(String.format("%d,%s,%s\n", longId, label, page.getNameNormalized()));
            } else if(StringUtils.startsWith(id, "children:")) {
                Long longParentId = Long.valueOf(StringUtils.substringAfterLast(id, ":"));
                Category category = categories.get(longParentId);
                Validate.notNull(category, "parent category is null");
                List<String> childIds = category.childArticles()
                        .map(Page::getId)
                        .map(Object::toString)
                        .collect(Collectors.toList());
                setLabel(childIds, label);
            } else {
                throw new IllegalArgumentException("Unparseable id " + id);
            }
        }
        mappingLogWriter.flush();
    }

    public void refreshStats() {
        LOG.info("refreshStats() invoked");
        StopWatch sw = StopWatch.createStarted();
        categories.values().forEach(Category::clearStats);
        articles.values().forEach(Page::clearStats);
        articles.values().forEach(Page::refreshStats);
        this.stats = createPageStats();
        this.indexes.values().forEach(FieldIndex::sort);
        sw.stop();
        LOG.info("refreshStats() done in {}", String.format("%.2fs", sw.getTime(TimeUnit.MILLISECONDS) / 1000.0));
    }

    private PagesStats createPageStats() {
        PagesStats result = new PagesStats(this.articles.size());
        articles.values().stream()
                .filter(val -> val.getAssignedLabel() != null)
                .forEach(val -> result.addLabel(val.getAssignedLabel()));
        return result;
    }

    private void loadPages() throws IOException {
        File input = new File(dataDir, "page.csv");
        LineIterator iter = FileUtils.lineIterator(input, StandardCharsets.UTF_8.name());
        while(iter.hasNext()) {
            String line = StringUtils.strip(iter.next());
            Optional<Page> page = createPage(line);
            page.ifPresent(val -> {
                if(val.getClass().equals(Page.class)) {
                    articles.put(val.getId(), val);
                } else {
                    categories.put(val.getId(), (Category) val);
                }
            });
        }
    }

    private void loadMappingLog() throws IOException {
        if(!this.mappingLogFile.exists()) return;
        LineIterator iter = FileUtils.lineIterator(this.mappingLogFile, StandardCharsets.UTF_8.name());
        while(iter.hasNext()) {
            String line = StringUtils.strip(iter.next());
            String[] values = StringUtils.split(line, ',');
            Long id = Long.valueOf(values[0]);
            String label = values[1];
            if(StringUtils.equals(label, "null")) label = null;
            Map<Long, ? extends Page> map = categories.containsKey(id) ? categories : articles;
            map.get(id).setLabel(label);
        }

    }

    private Optional<Page> createPage(String line) {
        if(StringUtils.isBlank(line)) return Optional.empty();
        String[] values = StringUtils.split(line, ',');
        Validate.isTrue(values.length == 4, "values length is %d", values.length);
        long id = Long.parseLong(values[0]);
        String title = StringUtils.stripStart(values[1], "'");
        int type = Integer.parseInt(values[2]);
        switch (type) {
            case 0: return Optional.of(new Page(id, title));
            case 1: return Optional.of(new Category(id, title));
            default: return Optional.empty();
        }
    }

    private void loadParents(String fileName, Function<Long, Page> getter) throws IOException {
        File input = new File(dataDir, fileName);
        LineIterator iter = FileUtils.lineIterator(input, StandardCharsets.UTF_8.name());
        while(iter.hasNext()) {
            String line = StringUtils.strip(iter.next());
            addParents(line, getter);
        }
    }

    private void addParents(String line, Function<Long, Page> func) {
        if(StringUtils.isBlank(line)) return;
        String[] values = StringUtils.split(line, ",", 2);
        long id = Long.parseLong(values[0]);
        List<Long> parents = parseIdList(values[1]);
        Page article = func.apply(id);
        if(article == null) return;
        parents.stream().map(categories::get).filter(Objects::nonNull).forEach(article::addParent);
    }

    private List<Long> parseIdList(String value) {
        Validate.isTrue(value.startsWith("v{"), "value is %s", value);
        String[] ids = StringUtils.split(StringUtils.strip(value, "v{}"), ",");
        return Arrays.stream(ids).map(Long::parseLong).collect(Collectors.toList());
    }

    public FieldIndex index(String sort) {
        if(StringUtils.isBlank(sort)) return this.indexes.get("id");
        return this.indexes.getOrDefault(sort, this.indexes.get("id"));
    }

    public Map<Long, Page> getArticles() {
        return articles;
    }

    public Map<Long, Category> getCategories() {
        return categories;
    }

    public PagesStats getStats() {
        return stats;
    }

    @Override
    public void close() {
        this.mappingLogWriter.close();
    }
}
