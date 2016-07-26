import React from 'react';
import { Modal } from 'react-bootstrap';
import { Button } from 'react-bootstrap';

class UpdateDialog extends React.Component {

    constructor(props) {
        super(props);
        this.state = { showModal: false };
        this.handleSubmit = this.handleSubmit.bind(this);
        this.open = this.open.bind(this);
        this.close = this.close.bind(this);
    }

    close() {
        this.setState({ showModal: false });
    }

    open() {
        this.setState({ showModal: true });
    }

    handleSubmit(e) {
        e.preventDefault();
        var updatedEmployee = {};
        this.props.attributes.forEach(attribute => {
            updatedEmployee[attribute] = React.findDOMNode(this.refs[attribute]).value.trim();
        });
        this.props.onUpdate(this.props.employee, updatedEmployee);
        this.close()
    }

    render() {
        var inputs = this.props.attributes.map(attribute =>
            <p key={this.props.employee.entity[attribute]}>
                <input type="text" placeholder={attribute}
                       defaultValue={this.props.employee.entity[attribute]}
                       ref={attribute} className="field" />
            </p>
        );

        var dialogId = "updateEmployee-" + this.props.employee.entity._links.self.href;

        return (
            <div>
                <Button bsStyle="primary" bsSize="small" onClick={this.open} >
                    Update
                </Button>
                <Modal show={this.state.showModal} onHide={this.close}>
                    <Modal.Header closeButton>
                        <Modal.Title>Update Employee</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <form>
                            {inputs}
                            <button onClick={this.handleSubmit}>Update</button>
                        </form>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this.close}>Close</Button>
                    </Modal.Footer>
                </Modal>
            </div>
        )
    }
}

export default UpdateDialog;
