from __future__ import print_function
import re

# Mermaid only allows alphanumeric identifiers
def cleanName(name):
    namepattern = r'[^-\d\w]'
    cleanedname = re.sub(namepattern, '', name)
    if not cleanedname:
        cleanedname = "UNKNOWN"
    return cleanedname

def printDiagram():
    print('```mermaid')
    print('erDiagram', '\n')
    for table in catalog.tables:
        print('  ' + cleanName(table.name) + ' {')
        for column in table.columns:
            print('    ' + cleanName(column.columnDataType.name) + ' ' + cleanName(column.name),
                end='')
            if column.isPartOfPrimaryKey():
                print(' PK', end='')
            elif column.isPartOfForeignKey():
                print(' FK', end='')
            elif column.isPartOfUniqueIndex():
                print(' UK', end='')
            print()
        print('  }')

    for table in catalog.tables:
        for childTable in table.referencingTables:
            if childTable in catalog.tables:
                print('  ' + cleanName(table.name) + ' ||--o{ ' +
                    cleanName(childTable.name) + ' : "foreign key"')
               
    print('```', '\n')

# Prints a markdown table with the provided data as content
def printTable(headers, data):
    print('|' + '|'.join(headers) + '|')
    print('|' + '|'.join(['---' for h in headers]) + '|')

    for row in data:
        print('|' + '|'.join(row) + '|')

    print('')

def printColumnList(columns):
    data = []
    for column in columns:
        column_name = '**' + column.name + '**' if column.isPartOfPrimaryKey() \
            else '*' + column.name + '*' if column.isPartOfForeignKey() \
            else column.name
        column_type = column.columnDataType.toString()
        column_remarks = '<ul>' + ''.join(map(lambda r: '<li>' + r + '</li>', column.remarks.splitlines())) + '</ul>' if column.remarks \
            else ''
        data.append([column_name, column_type, column_remarks])

    if data:
        print('#### Columns', '\n')
        printTable(['Name', 'Data type', 'Comments'], data)

def printForeignKeys(foreign_keys):
    data = []
    for fk in foreign_keys:
        for column_reference in fk.columnReferences:
            if fk.name and not fk.name.startswith('SCHCRWLR_'):
                fk_column_name = column_reference.foreignKeyColumn.name
                pk_column_name = column_reference.primaryKeyColumn.name
                referenced_table_name = fk.referencedTable.name
                data.append([fk_column_name, referenced_table_name, pk_column_name])

    if data:
        print('#### Foreign keys')
        printTable(['Column name', 'Referenced table', 'Referenced primary key'], data)

def printTableList():
    print('## Entity relationship diagram', '\n')
    printDiagram()
    
    print('## Tables', '\n')

    for table in catalog.tables:

        print('### ' + table.name, '\n')

        print('{{% expand title="Show details" expanded="false" center="true"%}}','\n')

        if table.remarks:
            print('#### Comments', '\n')
            print('\n'.join(map(lambda r: ' - ' + r, table.remarks.splitlines())), '\n')

        if table.columns:
            printColumnList(table.columns)

        if table.hasPrimaryKey():
            print('#### Primary Key', '\n')
            for column in table.primaryKey.constrainedColumns:
                print(" - " + column.name)

        if table.importedForeignKeys:
            printForeignKeys(table.importedForeignKeys)

        print('{{% /expand %}}')

    print('')

printTableList()
