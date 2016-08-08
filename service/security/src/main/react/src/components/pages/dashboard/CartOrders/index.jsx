import React from 'react';
import AsyncElement from '../../../common/AsyncElement';

var PreCartOrders = React.createClass({

  mixins: [ AsyncElement ],

  bundle: require('bundle?lazy!./CartOrders.jsx'),

  preRender: function () {
  	return <div></div>;
  }
});

export default PreCartOrders;