import React from "react";
import UpdateDialog from "../common/UpdateDialog"

class OrderIncident extends React.Component {

    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
    }

    handleDelete() {
        this.props.onDelete(this.props.orderIncident);
    }

  /**
   * - Link to order
   * - Description of activiti
   private boolean isManuallyApproved;

   private IncidentType type;
   private String status;
   private String description;
   private String cartOrderSystemQualifier;
   */
    render() {
        return (
            <tr>
              <td><a target="_blank" href={this.props.orderIncident.entity.cartOrder.url}>{this.props.orderIncident.entity.cartOrder.shoppingCartName}
                - {this.props.orderIncident.entity.cartOrder.cartOrderSystemId}</a></td>
              <td>{this.props.orderIncident.entity.type}</td>
              <td>{this.props.orderIncident.entity.isManuallyApproved}</td>
              <td><span style={{width: 100 + "px"}}>{this.props.orderIncident.entity.status}</span></td>

              <td>{this.props.orderIncident.entity.description}</td>
              <td>{this.props.orderIncident.entity.log}</td>
            </tr>
        )
    }
}
export default OrderIncident
