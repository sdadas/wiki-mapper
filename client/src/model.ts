
type ExpandType = 'CHILDREN' | 'CATEGORY_PARENTS' | 'ARTICLE_PARENTS';

type ShowType = 'ALL' | 'LABELLED' | 'UNLABELLED';

interface SearchRequest {
    search: string;
    expand: ExpandType;
    show?: ShowType;
    limit?: number;
    offset?: number;
    sort?: string;
}

interface ExpandRequest {
    nodeId: number;
    expand: ExpandType;
}

interface LabelRequest {
    ids: string[];
    label?: string;
}

interface PagesStats {
    allArticles: number,
    labelledArticles: number,
    labelledPercent: number,
    refreshing: boolean,
    labels: {[key: string]: any}
}