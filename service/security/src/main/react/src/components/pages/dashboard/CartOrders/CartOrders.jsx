import React, { PropTypes, Component } from 'react';
import {Pagination, Panel, Well, Button, PageHeader} from "react-bootstrap";

var rest = require('rest');
import StompClient from '../../../../routers/websocket-listener';
import Follow from '../../../../routers/follow';
import Client from '../../../../routers/client';
import When from 'when';
import CartOrder from "../../../../components/domain/CartOrder"
import CreateDialog from "../../../../components/common/CreateDialog"


// const root = 'http://localhost:8080/api';
const root = '/api';
const HARD_CODED_PROCESS_DEFINITION_KEY = "eu_fraud_wary_process";


class CartOrders extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      cartOrders: [],
      attributes: [],
      page: 1,
      pageSize: 10,
      activePage: 1,
      links: {}
    };
    this.updatePageSize = this.updatePageSize.bind(this);
    this.onCreate = this.onCreate.bind(this);
    this.onUpdate = this.onUpdate.bind(this);
    this.onDelete = this.onDelete.bind(this);
    this.onNavigate = this.onNavigate.bind(this);
    this.refreshCurrentPage = this.refreshCurrentPage.bind(this);
    this.refreshAndGoToLastPage = this.refreshAndGoToLastPage.bind(this);
    this.handleNavFirst = this.handleNavFirst.bind(this);
    this.handleNavPrev = this.handleNavPrev.bind(this);
    this.handleNavNext = this.handleNavNext.bind(this);
    this.handleNavLast = this.handleNavLast.bind(this);
    this.changePageSize = this.changePageSize.bind(this);
    this.changePageNumber = this.changePageNumber.bind(this);
  }

  componentDidMount() {
    this.loadFromServer(this.state.pageSize);
    StompClient.register([
      {route: '/topic/newCartOrder', callback: this.refreshAndGoToLastPage},
      {route: '/topic/updateCartOrder', callback: this.refreshCurrentPage},
      {route: '/topic/deleteCartOrder', callback: this.refreshCurrentPage}
    ]);
  }

  loadFromServer(pageSize) {
    console.log("Loading from server");
    var followed = Follow(Client, root, [
      {rel: 'cartOrders', params: {size: pageSize, page: this.state.page.number}}]
    ).then(cartOrderCollection => {
      return Client({
        method: 'GET',
        path: cartOrderCollection.entity._links.profile.href,
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
          if (typeof schema.entity.properties[property] == "undefined" || schema.entity.properties[property].hasOwnProperty('$ref')) {
            delete schema.entity.properties[property];
          }
        });

        this.schema = schema.entity;
        this.links = cartOrderCollection.entity._links;
        return cartOrderCollection;
        // end::json-schema-filter[]
      });
    }).then(cartOrderCollection => {
      this.page = cartOrderCollection.entity.page;
      return cartOrderCollection.entity._embedded.cartOrders.map(cartOrder =>
          Client({
            method: 'GET',
            path: cartOrder._links.self.href
          })
      );
    }).then(cartOrderPromises => {
      return When.all(cartOrderPromises);
    });

    followed.done(cartOrders => {
      this.setState({
        page: this.page,
        cartOrders: cartOrders,
        attributes: Object.keys(this.schema.properties),
        pageSize: pageSize,
        links: this.links
      });
    });

    console.log("Loading all of the tasks");
    this.refreshTasks();
  }

  refreshTasks() {
    var self = this;
    rest({
      path: '/query/tasks',
      "Content-type": "application/json",
      headers: {'Content-type': 'application/json'},
      entity: JSON.stringify({
        processDefinitionKey: HARD_CODED_PROCESS_DEFINITION_KEY
      })
    }).then(function (tasks) {
      self.setState({tasks: JSON.parse(tasks.entity).data})
    });
  }

  completeTask(taskId) {
    console.log("Loading task " + taskId + " from server");
    rest({
      method: "POST",
      path: "runtime/tasks/" + taskId,
      entity: JSON.stringify({
        action: "complete"
      }),
      headers: {'Accept': 'application/json', 'Content-type': 'application/json'}
    }).then(this.loadFromServer(this.state.pageSize));

  }

  // tag::on-create[]
  onCreate(newCartOrder) {
    Follow(Client, root, ['cartOrders']).done(response => {
      console.log("Posting entity " + JSON.stringify(newCartOrder));
      Client({
        method: 'POST',
        path: response.entity._links.self.href,
        entity: newCartOrder,
        headers: {'Content-Type': 'application/json'}
      })
    })
  }
  // end::on-create[]

  // tag::on-update[]
  onUpdate(cartOrder, updatedCartOrder) {
    Client({
      method: 'PUT',
      path: cartOrder.entity._links.self.href,
      entity: updatedCartOrder,
      headers: {
        'Content-Type': 'application/json',
        'If-Match': cartOrder.headers.Etag
      }
    }).done(response => {
      /* Let the websocket handler update the state */
    }, response => {
      if (response.status.code === 403) {
        alert('ACCESS DENIED: You are not authorized to update ' +
            cartOrder.entity._links.self.href);
      }
      if (response.status.code === 412) {
        alert('DENIED: Unable to update ' + cartOrder.entity._links.self.href +
            '. Your copy is stale.');
      }
    });
  }
  // end::on-update[]

  // tag::on-delete[]
  onDelete(cartOrder) {
    Client({method: 'DELETE', path: cartOrder.entity._links.self.href}
    ).done(response => {/* let the websocket handle updating the UI */},
        response => {
          if (response.status.code === 403) {
            alert('ACCESS DENIED: You are not authorized to delete ' +
                cartOrder.entity._links.self.href);
          }
        });
  }
  // end::on-delete[]

  onNavigate(navUri) {
    Client({
      method: 'GET',
      path: navUri
    }).then(cartOrderCollection => {
      this.links = cartOrderCollection.entity._links;
      this.page = cartOrderCollection.entity.page;

      return cartOrderCollection.entity._embedded.cartOrders.map(cartOrder =>
          Client({
            method: 'GET',
            path: cartOrder._links.self.href
          })
      );
    }).then(cartOrderPromises => {
      return When.all(cartOrderPromises);
    }).done(cartOrders => {
      this.setState({
        page: this.page,
        cartOrders: cartOrders,
        attributes: Object.keys(this.schema.properties),
        pageSize: this.state.pageSize,
        links: this.links
      });
    });
  }

  updatePageSize(pageSize, pageNumber) {
    if (pageSize !== this.state.pageSize || pageNumber > 0) {
      this.loadFromServer(pageSize);
    }
  }

  // tag::websocket-handlers[]
  refreshAndGoToLastPage(message) {
    Follow(Client, root, [{
      rel: 'cartOrders',
      params: {size: this.state.pageSize}
    }]).done(response => {
      if (response.entity._links.last !== undefined) {
        this.onNavigate(response.entity._links.last.href);
      } else {
        this.onNavigate(response.entity._links.self.href);
      }
    })
  }

  refreshCurrentPage(message) {
    Follow(Client, root, [{
      rel: 'cartOrders',
      params: {
        size: this.state.pageSize,
        page: this.state.page.number
      }
    }]).then(cartOrderCollection => {
      this.links = cartOrderCollection.entity._links;
      this.page = cartOrderCollection.entity.page;

      return cartOrderCollection.entity._embedded.cartOrders.map(cartOrder => {
        return Client({
          method: 'GET',
          path: cartOrder._links.self.href
        })
      });
    }).then(cartOrderPromises => {
      return When.all(cartOrderPromises);
    }).then(cartOrders => {
      this.setState({
        page: this.page,
        cartOrders: cartOrders,
        attributes: Object.keys(this.schema.properties),
        pageSize: this.state.pageSize,
        links: this.links
      });
    });
    this.refreshTasks();
  }
  // end::websocket-handlers[]
  changePageNumber(event, eventKey) {
    this.setState({activePage: eventKey.e});
    this.updatePageSize(pageSize);
    /**
     var eventText = event.target.innerText;
     if(isNaN(eventText) {

    }
     var pager = this.refs.pager;
     var sizeCombo = React.findDOMNode(this.refs.pageSize);
     if(sizeCombo) {
      var pageSize = sizeCombo.value;
      this.setState({ page: pager.page });
      this.updatePageSize(pageSize, pager.page);
    }
     **/
  }


  changePageSize(e) {
    e.preventDefault();
    var sizeCombo = React.findDOMNode(this.refs.pageSize);
    if(sizeCombo) {
      var pageSize = sizeCombo.value;
      this.updatePageSize(pageSize, -1);
    }
  }


  handleNavFirst(e) {
    e.preventDefault();
    this.onNavigate(this.links.first.href);
  }

  handleNavPrev(e) {
    e.preventDefault();
    this.onNavigate(this.links.prev.href);
  }

  handleNavNext(e) {
    e.preventDefault();
    this.onNavigate(this.links.next.href);
  }

  handleNavLast(e) {
    e.preventDefault();
    this.onNavigate(this.links.last.href);
  }

  render() {
    //var pageInfo = this.props.page.hasOwnProperty("number") ?
    //     <h3>CartOrders - Page {this.state.page.number + 1} of {this.state.page.totalPages}</h3> : null;

    var cartOrders = this.state.cartOrders.map(cartOrder =>
        <CartOrder key={cartOrder.entity._links.self.href}
                       cartOrder={cartOrder}
                       tasks={this.state.tasks}
                       completeTask={this.completeTask}
                       attributes={this.state.attributes}
                       onUpdate={this.onUpdate}
                       onDelete={this.onDelete}/>
    );
    /**
     Source
     Transaction id link to Shopping cart order
     A link to the workflow process for the order
     Ship to name
     Shipping method
     Ship to country
     Payment method
     Amount
     Status
     Review reason
     Action - Refund - Ship
     **/
    return (
        <div>
          <div className="col-lg-12">
            <PageHeader>Cart Orders</PageHeader>
          </div>

          <div className="col-lg-12">
            <Panel header={<span>Order Incidents</span>} >
              <div>
                <div className="dataTable_wrapper">
                  <div id="dataTables-example_wrapper" className="dataTables_wrapper form-inline dt-bootstrap no-footer">

                    <div className="row">
                      <div className="col-sm-9">
                        <div className="dataTables_length" id="dataTables-example_length">
                          <label>
                            Show
                            <select ref="pageSize" name="dataTables-example_length" aria-controls="dataTables-example" className="form-control input-sm"
                                    defaultValue={this.props.pageSize} onChange={this.changePageSize}>
                              <option value="10">10</option>
                              <option value="25">25</option>
                              <option value="50">50</option>
                              <option value="100">100</option>
                            </select> entries</label>
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
                            <th className="sorting" tabIndex="0" aria-controls="dataTables-example" rowSpan="1" colSpan="1" aria-label="Order" style={ {width: 81} }>Source</th>
                            <th className="sorting" tabIndex="0" aria-controls="dataTables-example" rowSpan="1" colSpan="1" aria-label="Status" style={ {width: 51} }>Amount</th>
                            <th className="sorting" tabIndex="0" aria-controls="dataTables-example" rowSpan="1" colSpan="1" aria-label="Status" style={ {width: 131} }>Status</th>
                            <th className="sorting" tabIndex="0" aria-controls="dataTables-example" rowSpan="1" colSpan="1" aria-label="User" style={ {width: 131} }>User</th>
                            <th className="sorting" tabIndex="0" aria-controls="dataTables-example" rowSpan="1" colSpan="1" aria-label="Whitelist" style={ {width: 100} }>Tasks</th>
                          </tr>
                          </thead>
                          <tbody>
                          {cartOrders}
                          </tbody>
                        </table>
                      </div>
                    </div>
                    <div className="row">
                      <div className="col-sm-6">
                        <h3>Orders - Page {this.state.page.number + 1} of {this.state.page.totalPages}</h3>

                        <div className="dataTables_info" id="dataTables-example_info" role="status" aria-live="polite">
                          Showing {1 + ((this.state.page.number) * this.state.page.size)} to {Math.min(this.state.page.totalElements, (1 + this.state.page.number) * this.state.page.size)} of {this.state.page.totalElements} entries</div>
                      </div>
                      <div className="col-sm-6" pullRight >
                        <Pagination activePage={this.state.activePage} ref="pager"
                                    items={6} perPage={10}
                                    first={true} last={true}
                                    prev={true} next={true}
                                    onSelect={this.changePageNumber} />
                      </div>
                    </div>
                  </div>
                </div>
                <Well>
                  <h4>Usage Information</h4>
                </Well>
              </div>
            </Panel>
          </div>
        </div>
    );
  }

}
export default CartOrders

