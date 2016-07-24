import React, { PropTypes, Component } from 'react';
import {Pagination, Panel, Well, Button, PageHeader} from "react-bootstrap";

import StompClient from '../../../../routers/websocket-listener';
import Follow from '../../../../routers/follow';
import Client from '../../../../routers/client';
import When from 'when';
import Employee from "../../../../components/domain/Employee"

// const root = 'http://localhost:8080/api';
const root = '/api';


class Tables extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      employees: [],
      attributes: [],
      page: 1,
      pageSize: 2,
      pagesSize: 10,
      links: {}
    };

  }

  componentDidMount() {
    this.loadFromServer(this.state.pageSize);
    StompClient.register([
      {route: '/topic/newEmployee', callback: this.refreshAndGoToLastPage},
      {route: '/topic/updateEmployee', callback: this.refreshCurrentPage},
      {route: '/topic/deleteEmployee', callback: this.refreshCurrentPage}
    ]);
  }

  loadFromServer(pageSize) {
    var followed = Follow(Client, root, [
      {rel: 'employees', params: {size: pageSize}}]
    ).then(employeeCollection => {
      return Client({
        method: 'GET',
        path: employeeCollection.entity._links.profile.href,
        headers: {'Accept': 'application/schema+json'}
      }).then(schema => {
        // tag::json-schema-filter[]
        /**
         * Filter unneeded JSON Schema properties, like uri references and
         * subtypes ($ref).
         */
        Object.keys(schema.entity.properties).forEach(function (property) {
          if (schema.entity.properties[property].hasOwnProperty('format') &&
              schema.entity.properties[property].format === 'uri') {
            delete schema.entity.properties[property];
          }
          if (schema.entity.properties[property].hasOwnProperty('$ref')) {
            delete schema.entity.properties[property];
          }
        });

        this.schema = schema.entity;
        this.links = employeeCollection.entity._links;
        return employeeCollection;
        // end::json-schema-filter[]
      });
    }).then(employeeCollection => {
      this.page = employeeCollection.entity.page;
      return employeeCollection.entity._embedded.employees.map(employee =>
          Client({
            method: 'GET',
            path: employee._links.self.href
          })
      );
    }).then(employeePromises => {
      return When.all(employeePromises);
    });

    followed.done(employees => {
      this.setState({
        page: this.page,
        employees: employees,
        attributes: Object.keys(this.schema.properties),
        pageSize: pageSize,
        links: this.links
      });
    });
  }
  
  render() {
    //var pageInfo = this.props.page.hasOwnProperty("number") ?
    //     <h3>Employees - Page {this.props.page.number + 1} of {this.props.page.totalPages}</h3> : null;

    var employees = this.state.employees.map(employee =>
        <Employee key={employee.entity._links.self.href}
                  employee={employee}
                  attributes={this.props.attributes}
                  onUpdate={this.props.onUpdate}
                  onDelete={this.props.onDelete}/>
    );
    return (

      <div>
        <div className="col-lg-12"> 
          <PageHeader>Tables</PageHeader> 
        </div>

        <div className="col-lg-12"> 
        	<Panel header={<span>DataTables Advanced Tables</span>} >
       			<div> 
       				<div className="dataTable_wrapper">
                <div id="dataTables-example_wrapper" className="dataTables_wrapper form-inline dt-bootstrap no-footer">
                  
                  <div className="row">
                    <div className="col-sm-9">
                      <div className="dataTables_length" id="dataTables-example_length">
                        <label>Show <select name="dataTables-example_length" aria-controls="dataTables-example" className="form-control input-sm"><option value="10">10</option><option value="25">25</option><option value="50">50</option><option value="100">100</option></select> entries</label>
                      </div>
                    </div>
                    <div className="col-sm-3">
                      <div id="dataTables-example_filter" className="dataTables_filter">
                        <label>Search:<input type="search" className="form-control input-sm" placeholder="" aria-controls="dataTables-example" /></label>
                      </div>
                    </div>
                  </div>

                  <div className="row">
                    <div className="col-sm-12">
                      <table className="table table-striped table-bordered table-hover dataTable no-footer" id="dataTables-example" role="grid" aria-describedby="dataTables-example_info">
                        <thead>
                          <tr role="row">
                            <th className="sorting_asc" tabIndex="0" aria-controls="dataTables-example" rowSpan="1" colSpan="1" aria-label="First Name" aria-sort="ascending" style={ {width: 265} }>First Name</th>
                            <th className="sorting" tabIndex="0" aria-controls="dataTables-example" rowSpan="1" colSpan="1" aria-label="Last Name" style={ {width: 321} }>Last Name</th><th className="sorting" tabIndex="0" aria-controls="dataTables-example" rowSpan="1" colSpan="1" aria-label="Last Name" style={ {width: 299} }>Description</th>
                            <th className="sorting" tabIndex="0" aria-controls="dataTables-example" rowSpan="1" colSpan="1" aria-label="Description: activate to sort column ascending" style={ {width: 231} }>Description</th>
                            <th className="sorting" tabIndex="0" aria-controls="dataTables-example" rowSpan="1" colSpan="1" aria-label="Update: activate to sort column ascending" style={ {width: 231} }>Update</th>
                            <th className="sorting" tabIndex="0" aria-controls="dataTables-example" rowSpan="1" colSpan="1" aria-label="Delete" style={ {width: 180} }>Delete</th></tr>
                        </thead>
                        <tbody>
                        {employees}
                        </tbody>
                      </table>
                    </div>
                  </div>
                  <div className="row">
                    <div className="col-sm-6">
                      <div className="dataTables_info" id="dataTables-example_info" role="status" aria-live="polite">Showing 1 to 10 of 57 entries</div>
                    </div>
                    <div className="col-sm-6" pullRight >
                      <Pagination activePage={1}
                        items={6} perPage={10} 
                        first={true} last={true}
                        prev={true} next={true}
                        onSelect={ (pageNumber) => {} } />  
                    </div>
                  </div>
                </div>
              </div>
             	<Well> 
                <h4>DataTables Usage Information</h4> 
                <p>DataTables is a very flexible, advanced tables plugin for jQuery. In SB Admin, we are using a specialized version of DataTables built for Bootstrap 3. We have also customized the table headings to use Font Awesome icons in place of images. For complete documentation on DataTables, visit their website at <a target="_blank" href="https://datatables.net/">'https://datatables.net/'</a>.</p> 
                <Button bsSize="large" block href="https://datatables.net/">View DataTables Documentation</Button> 
              </Well> 
            </div>
          </Panel>
        </div>

        <div className="row ng-scope"> 
          <div className="col-lg-6"> 
            <Panel header={<span>Kitchen Sink </span>} >
                <div className="table-responsive"> 
                  <table className="table table-striped table-bordered table-hover"> 
                    <thead> <tr> <th># </th><th>First Name </th><th>Last Name </th><th>Username   </th></tr></thead>
                    <tbody> <tr> <td>1 </td><td>Mark </td><td>Otto </td><td>@mdo  </td></tr><tr> <td>2 </td><td>Jacob </td><td>Thornton </td><td>@fat  </td></tr><tr> <td>3 </td><td>Larry </td><td>the Bird </td><td>@twitter   </td></tr></tbody>
                  </table>
                </div>
            </Panel>
          </div>
          <div className="col-lg-6"> 
            <Panel header={<span>Basic Table</span>} >
                <div className="table-responsive"> 
                  <table className="table"> 
                    <thead> <tr> <th># </th><th>First Name </th><th>Last Name </th><th>Username   </th></tr></thead>
                    <tbody> <tr> <td>1 </td><td>Mark </td><td>Otto </td><td>@mdo  </td></tr><tr> <td>2 </td><td>Jacob </td><td>Thornton </td><td>@fat  </td></tr><tr> <td>3 </td><td>Larry </td><td>the Bird </td><td>@twitter   </td></tr></tbody>
                  </table> 
                </div>
            </Panel>
          </div>
        </div>

        <div className="row ng-scope"> 
          <div className="col-lg-6"> 
            <Panel header={<span>Striped Rows </span>} >
                <div className="table-responsive"> 
                  <table className="table table-striped"> 
                    <thead> <tr> <th># </th><th>First Name </th><th>Last Name </th><th>Username   </th></tr></thead>
                    <tbody> <tr> <td>1 </td><td>Mark </td><td>Otto </td><td>@mdo  </td></tr><tr> <td>2 </td><td>Jacob </td><td>Thornton </td><td>@fat  </td></tr><tr> <td>3 </td><td>Larry </td><td>the Bird </td><td>@twitter   </td></tr></tbody>
                  </table> 
                </div> 
            </Panel>
          </div>
          <div className="col-lg-6"> 
            <Panel header={<span>Bordered Table </span>} >
                <div className="table-responsive table-bordered"> 
                  <table className="table"> 
                    <thead> <tr> <th># </th><th>First Name </th><th>Last Name </th><th>Username   </th></tr></thead>
                    <tbody> <tr> <td>1 </td><td>Mark </td><td>Otto </td><td>@mdo  </td></tr><tr> <td>2 </td><td>Jacob </td><td>Thornton </td><td>@fat  </td></tr><tr> <td>3 </td><td>Larry </td><td>the Bird </td><td>@twitter   </td></tr></tbody>
                  </table> 
                </div>
            </Panel>
          </div>
        </div>

        <div className="row ng-scope"> 
          <div className="col-lg-6"> 
            <Panel header={<span>Hover Rows </span>} >
                <div className="table-responsive"> 
                  <table className="table table-hover"> 
                    <thead> <tr> <th># </th><th>First Name </th><th>Last Name </th><th>Username   </th></tr></thead>
                    <tbody> <tr> <td>1 </td><td>Mark </td><td>Otto </td><td>@mdo  </td></tr><tr> <td>2 </td><td>Jacob </td><td>Thornton </td><td>@fat  </td></tr><tr> <td>3 </td><td>Larry </td><td>the Bird </td><td>@twitter   </td></tr></tbody>
                  </table> 
                </div>
            </Panel>
          </div>
          <div className="col-lg-6"> 
            <Panel header={<span>Context Classes </span>} >
                <div className="table-responsive"> 
                  <table className="table"> 
                    <thead> <tr> <th># </th><th>First Name </th><th>Last Name </th><th>Username   </th></tr></thead>
                    <tbody> <tr className="success"> <td>1 </td><td>Mark </td><td>Otto </td><td>@mdo  </td></tr><tr className="info"> <td>2 </td><td>Jacob </td><td>Thornton </td><td>@fat  </td></tr><tr className="warning"> <td>3 </td><td>Larry </td><td>the Bird </td><td>@twitter  </td></tr><tr className="danger"> <td>4 </td><td>John </td><td>Smith </td><td>@jsmith   </td></tr></tbody>
                  </table> 
                </div>
            </Panel>
          </div>
        </div>

      </div> 
    );
  }

}
export default Tables

