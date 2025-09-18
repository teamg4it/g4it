export interface CustomSidebarMenuForm {
    menu: {
        title?: string;
        subTitle?: string;
        description?: string;
        iconClass?: string;
        active?: boolean;
        hidden?: boolean;
        optional?: boolean;
        descriptionText?: string;
    }[];
    form: {
        name: string;
        label?: string;
        hintText?: string;
        type?: string;
        placeholder?: string;
        options?: {
            label?: string;
            value?: string;
        };
    }[];
}
