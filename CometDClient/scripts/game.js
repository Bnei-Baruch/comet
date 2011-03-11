(function($)
{
    $(document).ready(function()
    {
        // Check if there was a saved application state
        var stateCookie = org.cometd.COOKIE?org.cometd.COOKIE.get('game.push.button'):null;
        var state = stateCookie ? org.cometd.JSON.fromJSON(stateCookie) : null;
        var game = new Game(state);

        // restore some values
        if (state)
        {
            $('#username').val(state.username);
        }

        // Setup UI
        game.join();

        $('#pushButton').mousedown(game.pushButtonPressed);
        $('#pushButton').mouseup(game.pushButtonReleased);
        $('#pushButton').mouseleave(game.pushButtonReleased);
		
    });

	// Dependency on the config structure.
    function Game(state, defaultChannel)
    {
        var _self = this;
        var _wasConnected = false;
        var _connected = false;
        var _username;
        var _disconnecting;
        var _pushingButtonSubscriptionPressed;
        var _pushingButtonSubscriptionReleased;
        var _membersSubscription;
		var _channel = defaultChannel ? defaultChannel : "/pushGame";
		var _mouseState = false;
		var _nowPressing = 0;
		var _sendMemberJoin = false;
		var _queueCount = -1;

        this.join = function(username)
        {
            _disconnecting = false;
            _username = username;
            if (config && config.username)
            {
            	_username = config.username;
            }
            else if (!_username)
            {
                alert('Please enter a username');
                return;
            }

            //var cometdURL = location.protocol + "//" + location.host + config.contextPath + "/cometd";
			//if(location.protocol == "file:")
			var cometdURL = "http://" + config.contextPath + "/cometd";
				
            $.cometd.configure({
                url: cometdURL,
                //logLevel: 'debug'//,
                //Cross origin sharing problems in HTTP
                //requestHeaders: {"username":config.username, "verify":config.verify}
            });
            
            $.cometd.handshake({"username":config.username, "verify":config.verify});
        };

        this.leave = function()
        {
            //$.cometd.batch(function()
            //{
            
            _unsubscribe();
            
            //});
            
            $.cometd.disconnect();

            _username = null;
            _disconnecting = true;
        };

		this.stillHolding = function()
		{
			if (_mouseState)
			{
				$.cometd.publish(_channel + "/holding", {
	            	user: _username
	            	},
	            	{ username:config.username, verify:config.verify });
	            
	            var id = setTimeout(_self.stillHolding, 1000);
			}
		}

        this.pushButtonPressed = function()
        {
        	if (!_mouseState)
        	{
	            $.cometd.publish(_channel + "/pressed", {
	            	user: _username
	            	},
	            	{ username:config.username, verify:config.verify });
	            _mouseState = true;
	            
	            var id = setTimeout(_self.stillHolding, 1000);
            }
        };
        
        this.pushButtonReleased = function()
        {
        	if (_mouseState)
        	{
	            $.cometd.publish(_channel + "/released", {
	            	user: _username
	            	},
	            	{ username:config.username, verify:config.verify });
	            _mouseState = false;
            }
        };
        
        this.otherPressed = function(message)
        {
        	var id = 'member_element_' + message.data.user;
        	if ($('#'+id).length == 0)
        	{
            	$('#members').append('<span id=\''+id+'\'>'+message.data.user+'</span><br>');
            }
            $('#'+id).toggleClass('green');
        	_nowPressing++;
        	_self.updatePressing();
        };
        
        this.otherReleased = function(message)
        {
        	var id = 'member_element_' + message.data.user;
        	if ($('#'+id).length == 0)
        	{
            	$('#members').append('<span id=\''+id+'\'>'+message.data.user+'</span><br>');
            }
            $('#'+id).toggleClass('green');
            _nowPressing--;
            _self.updateReleasing();
        };
        
        this.receiveCounts = function(message)
        {
        	if (message.data.total)
        	{
        		var total = message.data.total;
        		if (_sendMemberJoin)
        		{
        			total--;
        			it (total < 0)
        				total = 0;
        			_sendMemberJoin = false;
        		}

        		$('#totalCount').html(message.data.total);
        	}
        	
        	if (message.data.connected)
        		$('#connectedCount').html(message.data.connected);
        		        		
        	_self.updateQueueCount(message);
        }

        this.updatePressing = function()
        {
        	$('#nowCount').html(_nowPressing);
        };
        
        this.updateReleasing = function()
        {
        	$('#nowCount').html(_nowPressing);

        	var theint = parseInt($("#totalCount").html());
			theint++;
			$("#totalCount").html(theint);
        };

        /**
         * Updates the members list.
         * This function is called when a message arrives on channel /<default>/members
         */
        this.updateQueueCount = function(message)
        {
        	if (_queueCount == -1 && message.data.connected)
        			$('#youConnectedCount').html(message.data.connected)
        
           	if (_queueCount == -1 && message.data.queueCount)
        		_queueCount = message.data.queueCount;
        
			if (message.data.leaveQueueCount && _queueCount != -1 &&
					message.data.leaveQueueCount < _queueCount) {
				var theint = parseInt($("#youConnectedCount").html());
				theint--;
				if (theint < 1)
					theint = 1;
				$("#youConnectedCount").html(theint);
			}
			
			if (parseInt($("#youConnectedCount").html()) > 
					parseInt($("#connectedCount").html()))
				$("#youConnectedCount").html($("#connectedCount").html());
        }
         
        this.members = function(message)
        {
        	_self.info(message);
        
        	if (message.data.membership == 'join')
        	{
        		var theint = parseInt($("#connectedCount").html());
				theint++;
				$("#connectedCount").html(theint);
        	}
        	else if (message.data.membership == 'leave')
        	{
        		var theint = parseInt($("#connectedCount").html());
				theint--;
				if (theint < 0)
					thrint = 0;

				$("#connectedCount").html(theint);
				
				_self.updateQueueCount(message);
        	}
        
        	var id = 'member_element_' + message.data.user;
        	if ($('#'+id).length == 0)
        	{
            	$('#members').append('<span id=\''+id+'\'>'+message.data.user+'</span><br>');
            }
        };
        
        this.info = function(message)
        {
        	$('#info').append(org.cometd.JSON.toJSON(message));
        }
		
        function _unsubscribe()
        {
            if (_pushingButtonSubscriptionPressed)
            {
				$.cometd.publish(_channel + '/members', {
                    user: _username,
                    membership: 'leave',
                    leaveQueueCount: _queueCount
                },
                { username:config.username, verify:config.verify });

                $.cometd.unsubscribe(_pushingButtonSubscriptionPressed);
            }
            _pushingButtonSubscriptionPressed = null;
            if (_pushingButtonSubscriptionReleased)
            {
            	$.cometd.unsubscribe(_pushingButtonSubscriptionReleased);
            }
            _pushingButtonSubscriptionReleased = null;

            if (_membersSubscription)
            {
                $.cometd.unsubscribe(_membersSubscription);
            }
            _membersSubscription = null;
            
            if (_getCounts)
            {
            	$.cometd.unsubscribe(_getCounts);
            }
            _getCounts = null;
        }

        function _subscribe()
        {
            _pushingButtonSubscriptionPressed = $.cometd.subscribe(_channel + '/pressed', _self.otherPressed, {username:config.username, verify:config.verify});
            _pushingButtonSubscriptionReleased = $.cometd.subscribe(_channel + '/released', _self.otherReleased, {username:config.username, verify:config.verify});
            _membersSubscription = $.cometd.subscribe(_channel + '/members', _self.members, {username:config.username, verify:config.verify});
            _getCounts = $.cometd.subscribe(_channel + '/getCounts', _self.receiveCounts, {username:config.username, verify:config.verify}); 
			
			//$.cometd.publish(_channel + '/members', {
			//	user: _username,
			//	membership: 'join',
			//},
            //{ username:config.username, verify:config.verify });
        }

        function _connectionInitialized()
        {
            // first time connection for this client, so subscribe tell everybody.
            //$.cometd.batch(function()
            //{
                _subscribe();
            //});
        }

        function _connectionEstablished()
        {
            // connection establish (maybe not for first time), so just
            // tell local user and update membership
            
            _self.info({
                data: {
                    user: 'system',
                    chat: 'Connection to Server Opened'
                }
            });
            
            $.cometd.publish(_channel + '/members', {
                user: _username,
                membership: 'join'
            },
            { username:config.username, verify:config.verify });
            _self._sendMemberJoin = true;
        }

        function _connectionBroken()
        {
            _self.info({
                data: {
                    user: 'system',
                    chat: 'Connection to Server Broken'
                }
            });
            
            $('#members').empty();
        }

        function _connectionClosed()
        {
            _self.info({
                data: {
                    user: 'system',
                    chat: 'Connection to Server Closed'
                }
            });
        }
        
        function _connectionUnresolved()
        {
            _self.info({
                data: {
                    user: 'system',
                    chat: 'Connection to Server Unresolved'
                }
            });
        }

        function _metaConnect(message)
        {
            if (_disconnecting)
            {
                _connected = false;
                _connectionClosed();
            }
            else
            {
                _wasConnected = _connected;
                _connected = message.successful === true;
                if (!_wasConnected && _connected)
                {
                    _connectionEstablished();
                }
                else if (_wasConnected && !_connected)
                {
                    _connectionBroken();
                }
                //else
                //{
                //	_connectionUnresolved();
                //}
            }
        }

        function _metaHandshake(message)
        {
            if (message.successful)
            {
                _connectionInitialized();
            }
        }
        
        function _metaSubscribed(message)
        {
        	if (message.successful)
        	{
        		if (message.subscription == _channel + '/getCounts')
        		{
        			$.cometd.publish('/service' + _channel + '/getCounts', {},
            			{ username:config.username, verify:config.verify });
        		}
        		//alert(org.cometd.JSON.fromJSON(message));
        	}
        }

        $.cometd.addListener('/meta/handshake', _metaHandshake);
        $.cometd.addListener('/meta/connect', _metaConnect);
		$.cometd.addListener('/meta/subscribe', _metaSubscribed);

        // Restore the state, if present
        if (state)
        {
            setTimeout(function()
            {
                // This will perform the handshake
                _self.join(state.username);
            }, 0);
        }

		function _onUnload()
        {
        	_self.leave();
        
            if ($.cometd.reload)
            {
                $.cometd.reload();
                // Save the application state only if the user was chatting
                if (_wasConnected && _username)
                {
                    var expires = new Date();
                    expires.setTime(expires.getTime() + 5 * 1000);
                    org.cometd.COOKIE.set('game.push.button', org.cometd.JSON.toJSON({
                        username: _username,
                    }), { 'max-age': 5, expires: expires });
                }
            }
            else
            {
                $.cometd.disconnect();
            }
        }
        
        if (window.onbeforeunload == null)
        	window.onbeforeunload = _onUnload;
        else
        {
        	var ahandle = window.onbeforeunload;
        	window.onbeforeunload = function() { ahandle(); _onUnload(); }
       	}

        //window.onunload = _onUnload;
        //window.onended = _onUnload;
        $(window).unload(_onUnload);
        
    }

})(jQuery);
