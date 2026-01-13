$(document).ready(function() {
	var $userRegister = $("#userRegister");

	if ($userRegister.length) {   // prevents errors on pages without the form
		$userRegister.validate({
			rules: {
				name: { required: true },
				mobileNumber: { required: true },
				email: { required: true },
				address: { required: true },
				city: { required: true },
				state: { required: true },
				pincode: { required: true },
				password: { required: true },
				
			},
			messages: {
				name: { required: 'Name required' },
				mobileNumber: { required: 'mobile No required' },
				email: { required: 'email required' },
				address: { required: 'address required '},
				city: { required: 'city required'},
				state: { required: 'state required' },
				pincode: { required: 'pincode required' },
				password: { required: 'password required' },
				
			}
		});
	}
});

