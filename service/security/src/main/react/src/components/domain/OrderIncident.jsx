import React from "react";
import UpdateDialog from "../common/UpdateDialog"

class Employee extends React.Component {

    constructor(props) {
        super(props);
        this.handleDelete = this.handleDelete.bind(this);
    }

    handleDelete() {
        this.props.onDelete(this.props.orderIncident);
    }

    render() {
        return (
            <tr>
                <td>{this.props.orderIncident.entity.shoppingCartName}</td>
                <td>{this.props.orderIncident.entity.action}</td>
                <td>{this.props.orderIncident.entity.transactionId}</td>
                <td>{this.props.orderIncident.entity.customerEmail}</td>
                <td>{this.props.orderIncident.entity.amount}</td>
                <td>{this.props.orderIncident.entity.description}</td>
                <td>
                    <UpdateDialog toUpdate={this.props.orderIncident}
                                  attributes={this.props.attributes}
                                  onUpdate={this.props.onUpdate}/>
                </td>
                <td>
                    <button onClick={this.handleDelete}>Delete</button>
                </td>
            </tr>
        )
    }
}
export default Employee
