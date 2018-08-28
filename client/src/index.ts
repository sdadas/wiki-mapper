let $ = require("jquery");
window['jQuery'] = $;
window['$'] = $;
let popper = require("popper.js");
window['Popper'] = popper;

import "jqueryui";
import "jquery.fancytree";
import "jquery.fancytree/dist/modules/jquery.fancytree.table.js"
import "jquery.fancytree/dist/skin-win8-n/ui.fancytree.min.css";
import "jquery-contextmenu/dist/jquery.contextMenu.min.js";
import "jquery-contextmenu/dist/jquery.contextMenu.min.css";
import "bootstrap";
import 'bootstrap/dist/css/bootstrap.min.css';

import RestService from "./service";

interface MainFilters {
    search: string;
    show: ShowType;
    expand: ExpandType;
}

class MainView {

    private filtersForm: HTMLFormElement;
    private treeElement: HTMLDivElement;
    private searchButton: HTMLButtonElement;
    private selectAllButton: HTMLButtonElement;
    private deselectAllButton: HTMLButtonElement;
    private statsButton: HTMLButtonElement;
    private refreshButton: HTMLButtonElement;
    private statsModalBody: HTMLDivElement;
    private treeRoot: Fancytree.FancytreeNode;
    private treeWidget: Fancytree.Fancytree;
    private service: RestService;
    private debouncedSearch: Function;
    private searching: boolean = false;
    private hasMoreRows: boolean = true;

    constructor() {
        this.filtersForm = document.getElementById("filters") as HTMLFormElement;
        this.treeElement = document.getElementById("tree") as HTMLDivElement;
        this.searchButton = document.getElementById("searchButton") as HTMLButtonElement;
        this.selectAllButton = document.getElementById("selectAllButton") as HTMLButtonElement;
        this.deselectAllButton = document.getElementById("deselectAllButton") as HTMLButtonElement;
        this.statsButton = document.getElementById("statsButton") as HTMLButtonElement;
        this.refreshButton = document.getElementById("refreshButton") as HTMLButtonElement;
        this.statsModalBody = document.getElementById("statsModalBody") as HTMLDivElement;
        this.service = new RestService();
        this.treeRoot = this.initFancyTree();
        this.treeWidget = this.treeRoot.tree;
        this.debouncedSearch = this.debounce(() => this.search(), 500);
        this.initEvents();
        this.initContextMenu();
        this.search();
    }

    private filters(): MainFilters {
        const arr: any[] = $(this.filtersForm).serializeArray();
        const res: any = {};
        for (const el of arr) {
            res[el.name] = el.value;
        }
        return res;
    }

    private loadMoreRows(): void {
        if(!this.hasMoreRows) {
            console.log("no more rows");
            return;
        }
        this.search(1000, this.treeWidget.rootNode.children.length);
    }

    private search(limit: number=100, offset: number=0): void {
        if(this.searching) return;

        this.searching = true;
        const filters: MainFilters = this.filters();
        const request: SearchRequest = {limit: limit, offset: offset, ...filters};
        this.service.search(request)
            .then((data, status, xhr) => this.loadData(data, request.limit, request.offset))
            .catch((err) => this.onError(err));
    }

    private loadData(data: Fancytree.NodeData[], limit: number, offset: number): void {
        if(offset == 0) this.treeRoot.removeChildren();
        this.treeRoot.addChildren(data);
        this.searching = false;
        this.hasMoreRows = data.length >= limit;
    }

    private onError(err: any): void {
        console.log(err);
        this.searching = false;
    }

    private expand(input: any): void {
        input.result = new Promise<any>((resolve, reject) => {
            const filters: MainFilters = this.filters();
            this.service.expand({nodeId: input.node.key, expand: filters.expand})
                .then((data) => resolve(data))
                .catch((err) => reject());
        });
    }

    private label(label: string, id: string, clickedNode: any): void {
        let ids: string[] = [];
        let labelValue: string = label === "none" ? null : label;
        const selection: Fancytree.FancytreeNode[] = this.treeWidget.getSelectedNodes(true);
        if(selection.length > 0) {
            ids = selection.map(sel => sel.key);
            selection.forEach(node => $(node.tr).find(">td").eq(4).text(labelValue));
        } else {
            ids.push(id);
            clickedNode.closest("tr").find(">td").eq(4).text(labelValue);
        }
        this.service.label({ids: ids, label: labelValue});
    }

    private initFancyTree(): Fancytree.FancytreeNode {
        $(this.treeElement).fancytree({
            extensions: ["table"],
            checkbox: true,
            selectMode: 2,
            source: [],
            table: {
                indentation: 20,
                nodeColumnIdx: 1,
                checkboxColumnIdx: 0
            },
            lazyLoad: (event, data) => this.expand(data),
            select: (event, data) => console.log(event.type + ": " + data.node.isSelected() + " " + data.node),
            renderColumns: (event, data) => this.renderColumns(event, data)
        });
        return $(this.treeElement).fancytree("getRootNode");
    }

    private renderColumns(event: any, data: any): void {
        const node: any = data.node;
        const cols: JQuery = $(node.tr).find(">td");
        cols.eq(1).addClass("row-label-selectable");
        cols.eq(1).attr("data-id", node.key);
        if(node.data.childrenGroup) {
            cols.eq(2).text("");
            cols.eq(3).text("");
            cols.eq(4).text("");
            cols.eq(5).text("");
        } else {
            cols.eq(2).text(node.data.childArticles);
            cols.eq(3).text(node.data.labelledArticles);
            cols.eq(4).text(node.data.label);
            cols.eq(5).text(node.data.inheritedLabel);
        }
    }

    private selectAll(): void {
        const nodes: Fancytree.FancytreeNode[] = this.treeWidget.getRootNode().children;
        nodes.forEach(node => node.setSelected(true));
    }

    private deselectAll(): void {
        const nodes: Fancytree.FancytreeNode[] = this.treeWidget.getSelectedNodes(false);
        nodes.forEach(node => node.setSelected(false));
    }

    private showStats(): void {
        this.service.stats()
            .then((data) => this.statsModalBody.innerHTML = this.statsToHtml(data))
            .catch((err) => console.log(err));
    }

    private statsToHtml(stats: PagesStats): string {
        let rows: any[] = [];
        rows.push({"label": "All articles", "value": stats.allArticles});
        rows.push({"label": "Labelled articles", "value": stats.labelledArticles});
        rows.push({"label": "Labelled percent", "value": stats.labelledPercent + "%"});
        rows.push({"label": "Unlabelled articles", "value": stats.allArticles - stats.labelledArticles});
        let labels: any[] = [];
        for(const key of Object.keys(stats.labels)) {
            labels.push({"label": key, "value": stats.labels[key]});
        }
        labels.sort((o1, o2) => o2.value - o1.value);
        rows.push({"label":"----------------------", "value":""});
        rows = rows.concat(labels);

        if(stats.refreshing) {
            rows.push({"label":"----------------------", "value":""});
            rows.push({"label":"Stats are being refreshed", "value":""})
        }

        let elements: string[] = [];
        for(const row of rows) {
            elements.push(`<dt class="col-sm-6">${row.label}</dt><dd class="col-sm-6">${row.value}</dd>`);
        }
        return `<dl class="row">${elements.join('')}</dl>`
    }

    private refreshStats(): void {
        this.service.refresh().catch((err) => console.log(err));
    }

    private initEvents(): void {
        $(this.filtersForm).on("submit", (e: Event) => { e.preventDefault(); this.search(); });
        $(this.filtersForm).on("change", "input[type=radio].filters-input", () => this.search());
        $(this.searchButton).on("click", (e: Event) => { e.preventDefault(); this.search(); });
        $(this.selectAllButton).on("click", (e: Event) => { e.preventDefault(); this.selectAll(); });
        $(this.deselectAllButton).on("click", (e: Event) => { e.preventDefault(); this.deselectAll(); });
        $(this.statsButton).on("click", (e: Event) => { e.preventDefault(); this.showStats(); });
        $(this.refreshButton).on("click", (e: Event) => { e.preventDefault(); this.refreshStats(); });
        $(window).scroll(() => {
            const bottom: number = $(window).scrollTop() - ($(document).height() - window.innerHeight - 10);
            if(bottom > 0) {
                this.loadMoreRows();
            }
        });
    }

    private initContextMenu(): void {
        this.service.labels()
            .then((data) => this.createContextMenu(data))
            .catch((err) => console.log(err));
    }

    private createContextMenu(labels: any): void {
        const that = this;
        $.contextMenu({
            selector: ".row-label-selectable",
            callback: function(key, options) {
                let id: string = this.attr("data-id");
                that.label(key, id, this);
            },
            items: labels
        });
    }

    private debounce(func: Function, wait: number, immediate?: boolean) {
        let timeout;
        return () => {
            let context = this, args = arguments;
            clearTimeout(timeout);
            timeout = setTimeout(() => {
                timeout = null;
                if (!immediate) func.apply(context, args);
            }, wait);
            if (immediate && !timeout) func.apply(context, args);
        }
    }
}

new MainView();

