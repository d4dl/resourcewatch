'use strict';

var SockJS = require('sockjs-client'); // <1>
require('stompjs'); // <2>

function register(registrations) {
	// var socket = SockJS('http://localhost:8080/payroll'); // <3>
	var socket = SockJS('/payroll'); // <3>
	var stompClient = Stomp.over(socket);
	stompClient.connect({}, function(frame) {
		registrations.forEach(function (registration) { // <4>
			stompClient.subscribe(registration.route, registration.callback);
		});
	});
}

module.exports = {
	register: register
};

