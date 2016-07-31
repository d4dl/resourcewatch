import React from 'react';
import { Modal } from 'react-bootstrap';
import { Button } from 'react-bootstrap';

class CreateDialog extends React.Component {

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
        var newEmployee = {};
        this.props.attributes.forEach(attribute => {
            newEmployee[attribute] = React.findDOMNode(this.refs[attribute]).value.trim();
        });
        this.props.onCreate(newEmployee);
        this.props.attributes.forEach(attribute => {
            React.findDOMNode(this.refs[attribute]).value = ''; // clear out the dialog's inputs
        });
        this.close()
    }

    render() {
        var inputs = this.props.attributes.map(attribute =>
            <p key={attribute}>
                <input type="text" placeholder={attribute} ref={attribute} className="field" />
            </p>
        );
        return (
            <div>
                <Button bsStyle="primary" bsSize="medium" onClick={this.open} >
                    Create New
                </Button>
                <Modal show={this.state.showModal} onHide={this.close}>
                    <Modal.Header closeButton>
                        <Modal.Title>New</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <form>
                            {inputs}
                            <button onClick={this.handleSubmit}>Create</button>
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

export default CreateDialog;
