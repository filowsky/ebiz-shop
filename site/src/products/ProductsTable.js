import {DataGrid} from "@mui/x-data-grid";
import React from "react";

export function ProductsTable({products, onSelectedChange}) {
    const [selectionModel, setSelectionModel] = React.useState([]);

    return <div style={{height: 600, width: '100%'}}>
        <DataGrid
            rows={products}
            columns={columns}
            onSelectionModelChange={(newSelectionModel) => {
                onSelectedChange(newSelectionModel);
            }}
            selectionModel={selectionModel}

        />
    </div>
}

const columns = [
    {field: 'id', headerName: 'ID', width: 400, editable: false},
    {field: 'name', headerName: 'Name', width: 180, editable: true},
    {field: 'description', headerName: 'Description', width: 500, editable: true},
    {field: 'category', headerName: 'Category', width: 180, editable: true},
];