export const groupByCriterion = (arr: any[]) => {
    return arr.reduce((acc: any, obj: any) => {
        const key = transformCriterion(obj.criterion);
        if (!acc[key]) {
            acc[key] = [];
        }
        acc[key].push(obj);
        return acc;
    }, {});
};

export const transformCriterion = (criterion: string) => {
    return criterion.toLocaleLowerCase().replaceAll("_", "-");
};

//Below method is to remove the current workspace name from the equipment type
export const transformEquipmentType = (eqType: string, currentWorkspaceName: string) => {
    const lowerEqType = eqType?.toLowerCase()?.trim();
    const lowerPrefix = currentWorkspaceName?.toLowerCase()?.trim();

    if (lowerEqType.startsWith(lowerPrefix)) {
        return eqType.slice(currentWorkspaceName.length + 1); // Remove only if it matches
    }

    return eqType;
};

export const groupByField = (arr: any[], field: string) => {
    return arr.reduce((acc: any, obj: any) => {
        const key = obj[field];
        if (!acc[key]) {
            acc[key] = [];
        }
        acc[key].push(obj);
        return acc;
    }, {});
};

export const sumByProperty = (arr: any[], property: string): number =>
    arr.reduce((n: number, obj: any) => n + obj[property], 0);
