import React from 'react';
import AsyncElement from '../../../common/AsyncElement';

var PreOrderIncidents = React.createClass({

  mixins: [ AsyncElement ],

  bundle: require('bundle?lazy!./OrderIncidents.jsx'),

  preRender: function () {
  	return <div></div>;
  }
});

export default PreOrderIncidents;