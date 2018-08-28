

export default class RestService {

    private static URL_BASE: string = 'http://localhost:8080';

    public search(request: SearchRequest): JQueryXHR {
        return this.request('/pages', request);
    }

    public expand(request: ExpandRequest): JQueryXHR {
        return this.request('/expand', request);
    }

    public label(request: LabelRequest): JQueryXHR {
        return this.request('/label', request);
    }

    public refresh(): JQueryXHR {
        return this.request("/refresh", null);
    }

    public stats(): JQueryXHR {
        return this.request("/stats", null, "GET");
    }

    public labels(): JQueryXHR {
        return this.request("/labels", null, "GET");
    }

    private request(url: string, data: any, method: string="POST") {
        const settings: JQueryAjaxSettings = {
            url: `${RestService.URL_BASE}${url}`,
            cache: false,
            contentType: 'application/json',
            dataType: 'json',
            method: method
        };
        if(data) {
            settings.data = JSON.stringify(data);
        }
        return jQuery.ajax(settings);
    }
}