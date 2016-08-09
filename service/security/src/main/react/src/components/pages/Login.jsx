import React from 'react';
import Router from 'react-router';
import {Panel, Input, Button} from 'react-bootstrap';

var LoginPage = React.createClass({

  getInitialState: function(){
    return {
      loginID: '',
      password: '',
      isSubmitted: false
    };
  },

  mixins: [Router.Navigation],

  render: function(){
  
    return <div className="col-md-4 col-md-offset-4">

        <div className="text-center">
          <h1 className="login-brand-text">Resource Watch</h1>
          <h3 className="text-muted">Created by <a href="http://d4dl.com">DeFord Logistics</a></h3>
        </div>

        <Panel header={<h3>Please Sign In</h3>} className="login-panel">

          <form action="/" role="form" method="POST" >
            <fieldset>
              <div className="form-group">
                <Input onChange={this.setLoginID} className="form-control" placeholder="Username" ref="loginID" type="text" autofocus="" name="username" />
              </div>

              <div className="form-group">
                <Input onChange={this.setPassword} className="form-control" placeholder="Password" ref="password" type="password" name="password" />
              </div>
              <Input type="checkbox" label="Remember Me" />
              <Button type="submit" bsSize="large" bsStyle="success" block>Login</Button>
              
            </fieldset>
          </form>

        </Panel>
        
      </div>
      

  },

  setLoginID: function(e) {

    this.setState({
      loginID: e.target.value,
      loginError: ''
    });

  },

  setPassword: function(e) {

    this.setState({
      password: e.target.value,
      loginError: ''
    });

  },

  handleLogin: function(e){
    var password = this.state.password;
    var username = this.state.loginID;
    $.ajax({
      url: '/login',
      type: 'POST',
      contentType: 'application/json; charset=UTF-8',
      dataType: 'json',
      data: JSON.stringify({username: username, password: password})
    })
    .success(function (result) {
      location.reload();
    });
    
    return false;
  }

});

export default LoginPage;