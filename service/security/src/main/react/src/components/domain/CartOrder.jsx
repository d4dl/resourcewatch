import React from "react";
import ConfirmWhitelistDialog from "../common/ConfirmWhitelistDialog"
import { Button, ButtonToolbar, ButtonGroup } from 'react-bootstrap';
import UpdateDialog from "../common/UpdateDialog"

class CartOrder extends React.Component {

  /**
   * @returns {XML}
   */

  render() {
    var whitelistButton = <span/>
    var taskButtons = this.props.tasks.map(task => {
      console.log("Creating task buttons");
      console.log("Creating button for task: " + task.name + " key: '" + task.processInstanceId + "'" + " '" + this.props.cartOrder.entity.processInstanceId + "'");
      if(task.processInstanceId == this.props.cartOrder.entity.processInstanceId) {
         console.log("Creating button for " + task.name);
         return <Button sStyle="primary" bsSize="small" onClick={this.props.completeTask.bind(this, task.id)}>{task.name}</Button>
      }
    });

    if(this.props.cartOrder.entity.ccLastFour && this.props.cartOrder.entity.email && !this.props.cartOrder.entity.isWhitelisted) {
      whitelistButton = <ConfirmWhitelistDialog toUpdate={this.props.cartOrder} attributes={this.props.attributes}
                                                onUpdate={this.props.onUpdate}/>
      console.log("Creating whitelist button");
      taskButtons[taskButtons.length] = whitelistButton;
    }
    console.log("Taskbuttons is " + taskButtons.length + " items long.");
    var url = this.props.cartOrder.entity.url || this.props.cartOrder.entity.cartEndpoint;
    var cartOrderLabel = this.props.cartOrder.entity.shoppingCartName + " - " + this.props.cartOrder.entity.cartOrderSystemId;
    return (
        <tr>
          <td><a target="_blank" href={url}>{cartOrderLabel}</a></td>
          <td>${(1*this.props.cartOrder.entity.amount).toFixed(2)}</td>
          <td><div style={{width: 210 + "px"}}>{this.props.cartOrder.entity.status}</div></td>

          <td><a target="_blank"
                 href={"https://pipl.com/search/?q=" + this.props.cartOrder.entity.email + "&l=&sloc=&in=5"}>{this.props.cartOrder.entity.email}</a>
          </td>
          <td>
            <ButtonToolbar>
              <ButtonGroup>
                {taskButtons}
            </ButtonGroup>
          </ButtonToolbar>
          </td>
        </tr>
    )
  }
}
export default CartOrder
