export class SearchFilter {

    public type: string;
    public id: string;
    public name: string;
    public options: any[];
    public value: any;
    public suggestions: any;
    public exclusive: boolean;

    constructor(
            type,
            id,
            name,
            options,
            suggestions
        ) {
            this.type = type;
            this.id = id;
            this.name = name;
            this.options = options;
            this.suggestions = suggestions;
            if (type === 'enum') {
                this.exclusive = false;
            }
        }
}
