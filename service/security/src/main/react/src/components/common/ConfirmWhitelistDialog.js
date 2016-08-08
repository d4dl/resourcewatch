import React from 'react';
import { Modal } from 'react-bootstrap';
import { Button } from 'react-bootstrap';


class ConfirmWhitelistDialog extends React.Component {

  constructor(props) {
    super(props);
    this.state = {showModal: false};
    this.handleSubmit = this.handleSubmit.bind(this);
    this.open = this.open.bind(this);
    this.close = this.close.bind(this);
  }

  close() {
    this.setState({showModal: false});
  }

  open() {
    this.setState({showModal: true});
  }

  handleSubmit(e) {
    e.preventDefault();
    var updatedCustomer = {};
    this.props.attributes.forEach(attribute => {
      updatedCustomer[attribute] = this.props.toUpdate.entity[attribute]
    });
    if (this.props.toUpdate.entity.status == null) {
      this.props.toUpdate.entity.status == "";
    }


    updatedCustomer['orderTag'] = "whitelisting";
    updatedCustomer['shouldWhitelist'] = true;
    this.props.onUpdate(this.props.toUpdate, updatedCustomer);
    this.close()
  }

  render() {
    var dialogId = "confirmWhitelist-" + this.props.toUpdate.entity._links.self.href;

    return (
        <div>
          <Button bsStyle="primary" bsSize="small" onClick={this.open}> Whitelist </Button> <Modal
            show={this.state.showModal} onHide={this.close}> <Modal.Header closeButton> <Modal.Title>Confirm
          whitelist</Modal.Title> </Modal.Header> <Modal.Body>
          <form>
            Are you sure you want to whitelist this customer?<br/> <Button
              onClick={this.handleSubmit}>Yes</Button>&nbsp;<Button onClick={this.close}>Cancel</Button>
          </form>
        </Modal.Body> <Modal.Footer> <Button onClick={this.close}>Close</Button> </Modal.Footer> </Modal>
        </div>
    )
  }
}

export default ConfirmWhitelistDialog;
