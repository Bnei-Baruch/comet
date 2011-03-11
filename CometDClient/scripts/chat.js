(function($)
{

/**
*
*  URL encode / decode
*  http://www.webtoolkit.info/
*
**/
 
var Url = {
 
 	// Checks if first character is of type %D0
 	isFirstCharEncoded : function (string) {
 		//alert(string.charAt(0));
 		//alert(string.charCodeAt(0));
 		return string.charCodeAt(0) == 37;
 	},
 
	// public method for url encoding
	encode : function (string) {
		return escape(this._utf8_encode(string));
	},
 
	// public method for url decoding
	decode : function (string) {
		return this._utf8_decode(unescape(string));
	},
 
	// private method for UTF-8 encoding
	_utf8_encode : function (string) {
		string = string.replace(/\r\n/g,"\n");
		var utftext = "";
 
		for (var n = 0; n < string.length; n++) {
 
			var c = string.charCodeAt(n);
 
			if (c < 128) {
				utftext += String.fromCharCode(c);
			}
			else if((c > 127) && (c < 2048)) {
				utftext += String.fromCharCode((c >> 6) | 192);
				utftext += String.fromCharCode((c & 63) | 128);
			}
			else {
				utftext += String.fromCharCode((c >> 12) | 224);
				utftext += String.fromCharCode(((c >> 6) & 63) | 128);
				utftext += String.fromCharCode((c & 63) | 128);
			}
 
		}
 
		return utftext;
	},
 
	// private method for UTF-8 decoding
	_utf8_decode : function (utftext) {
		var string = "";
		var i = 0;
		var c = c1 = c2 = 0;
 
		while ( i < utftext.length ) {
 
			c = utftext.charCodeAt(i);
 
			if (c < 128) {
				string += String.fromCharCode(c);
				i++;
			}
			else if((c > 191) && (c < 224)) {
				c2 = utftext.charCodeAt(i+1);
				string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
				i += 2;
			}
			else {
				c2 = utftext.charCodeAt(i+1);
				c3 = utftext.charCodeAt(i+2);
				string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
				i += 3;
			}
 
		}
 
		return string;
	}
 
}

	//util function last
	function last(element)
	{
		//if (!element || element.length == 0)
		//	return null;
		//return Object(
		return $(element.get(element.size()-1));
	}
	
	function get_navi_channel(naviElement)
	{
		var channel = naviElement.find('.navbar-title');
		
		channel = channel.text().replace(/^([^ ]*) /, "");
		
		return channel;
	}
	
	function _advancedClick()
	{
		//var a = $('#advancedInner');
		$('#advancedInner').css('display', 'block');
		$('#advanced').css('cursor', 'auto');
		$('#advanced').unbind('click');
	}
	
	function runTest()
	{
		testJS();
	}
	
	function testJS()
	{
		var testOk = true;
		
		var a = 'kuku kaka kiki'.replace(/^([^ ]*) /, "");
		
		if(a != 'kaka kiki')
			testOk = faslse
		
    	var b = 'kuku0 kuku1'.replace(/^([^ ]*) /, "");
    	
    	if (b != "kuku1")
    		testOk = false;
    	
    	if (!testEncodeDecode())
    		testOk = false;

		alert('Test ok:'+testOk);

    	return testOk;
	}
	
	function testEncodeDecode()
	{
		return Url.isFirstCharEncoded("%D4%A0")
			&& !Url.isFirstCharEncoded("kuku");
	}

	function testMembers()
	{
		var aMember = "kuku";
		var chatObj = new Chat();

		if(chatObj.member(aMember))
			return false;

		chatObj.memberJoined(aMember);
		
		if(!chatObj.member(aMember))
			return false;
		
		charObj.memberLeft(aMember);

		if(chatObj.member(aMember))
			return false;
		
		return true;
	}

    $(document).ready(function()
    {
    	//runTest();

    	var pathname = window.location.pathname;
    	var isMainPage = pathname == "/" || pathname == "/home";
    	
    	pathArray = window.location.pathname.split( '/' );
    	
    	var pageChannel = "home";
    	
    	if (!isMainPage)
    	{
    		if (config && config.defaultChannelName)
    			pageChannel = config.defaultChannelName;
    			
    		// Get channel name from URL
    		else if (Url.isFirstCharEncoded(pathArray[pathArray.length-1]))
    			pageChannel = Url.decode(pathArray[pathArray.length-1]);
    		else
    			pageChannel = pathArray[pathArray.length-1];
    	}
		else {
			pageChannel = get_navi_channel($('.navi-active'));
			
			var naviElems = $('.navi,.navi-active');
			
	        $.each(naviElems, function()
	        {
	        	$(this).click(
	        			function(){chat.changeChannels(get_navi_channel($(this)))}
	        		)
	        });
		}
    	
        // Check if there was a saved application state
        var stateCookie = org.cometd.COOKIE?org.cometd.COOKIE.get('org.cometd.demo.state'):null;
        var state = stateCookie ? org.cometd.JSON.fromJSON(stateCookie) : null;
        
		var chat = new Chat(state, pageChannel);

        // restore some values
        if (state)
        {
            $('#username').val(state.username);
            $('#useServer').attr('checked',state.useServer);
            $('#altServer').val(state.altServer);
        }

        // Setup UI
        if (config && config.username)
        {
        	$('#join').hide();
        	$('#joined').show();
        	
        	chat.join();
        }
        else
        {
        	$('#join').show();
        	$('#joined').hide();
        }

        $('#altServer').attr('autocomplete', 'off');
        $('#joinButton').click(function() { chat.join($('#username').val()); });
        $('#sendButton').click(chat.send);
        $('#leaveButton').click(chat.leave);
        $('#username').attr('autocomplete', 'off').focus();
        $('#username').keyup(function(e)
        {
            if (e.keyCode == 13)
            {
                chat.join($('#username').val());
            }
        });
 
        $('#phrase').attr('autocomplete', 'off');
						
		$('#advanced').click(_advancedClick);
		
		$('#closeAdvanced').click(function()
		{
			$('#advancedInner').css('display', 'none');
			$('#advanced').css('cursor', 'pointer');
			$('#advanced').click(_advancedClick);
			return false;
		});
		
		$('#private').click(function(){
			if ($('#toDiv').css('display') == 'block')
				$('#toDiv').css('display', 'none');
			else
			{
				$('#toDiv').css('display', 'block');
				$('#to').focus();
			}
		});
		
		$('#to').append('<option selected>'+config.labels.get('privateSpan')+'</option>');

		$('#to').blur(function(){
			var values = "";
            $.each($('#to').children(), function()
            {
            	if (this.selected)
                	values += this.textContent + ';';
            });
            
			$('#privateSpan').attr('value',values);
			$('#toDiv').css('display', 'none');
		});
    });

	// Dependency on the config structure.
    function Chat(state, defaultChannel)
    {
        var _self = this;
        var _wasConnected = false;
        var _connected = false;
        var _username;
        var _lastUser;
        var _disconnecting;
        var _chatSubscription;
        var _membersSubscription;
        var _privateChatSubscription
		var _channel = defaultChannel ? "/auth/"+defaultChannel : "/auth/chat/demo";
		var _channelName = defaultChannel ? defaultChannel : "auth/chat/demo";

		_initOverlay();
		_disableInputs();

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

			var cometdURL = "http://" + config.contextPath + "/cometd";
				
            var useServer = $('#useServer').attr('checked');
            if (useServer)
            {
                var altServer = $('#altServer').val();
                if (altServer.length == 0)
                {
                    alert('Please enter a server address');
                    return;
                }
                cometdURL = altServer;
            }

            $.cometd.configure({
                url: cometdURL,
                logLevel: 'debug'//,
                //Cross origin sharing problems in HTTP
                //requestHeaders: {"username":config.username, "verify":config.verify}
            });
            
            $.cometd.handshake({"username":config.username, "verify":config.verify});

            $('#join').hide();
            $('#joined').show();
            $('#phrase').focus();
        };

        this.leave = function()
        {
            $.cometd.batch(function()
            {
                _unsubscribe();
            });
            $.cometd.disconnect();

            $('#join').show();
            $('#joined').hide();
            $('#username').focus();
            $('#members').empty();
            _username = null;
            _lastUser = null;
            _disconnecting = true;
        };

        this.send = function()
        {		
            var phrase = $('#phrase');
            var text = phrase.val();
            phrase.val('');

            if (!text || !text.length) return;

            //var toList = $('#privateSpan').val().split(';');
            
            //if (toList[0] != config.labels.get('privateSpan'))
            //{
            //    $.cometd.publish('/service/privatechat', {
            //        to: $('#privateSpan').val(),
            //        user: _username,
            //        chat: text  
            //    },
            //    { username:config.username, verify:config.verify }
            //    );
            //}
            //else
            //{
                $.cometd.publish(_channel, {
                    user: _username,
                    chat: text
                },
                { username:config.username, verify:config.verify });
            //}
        };
        
        function _onMessageOpen(chatMessage)
        {
        	chatMessage.unbind('click');
        	chatMessage.css('background-color','#fafafa');
        	//chatMessage.append('<button>'+config.labels.get('privateMessage')+'</button>');
        	
        	//last(chatMessage.children()).click( function() {
        	//
        	//	chatMessage.unbind('mouseout');

        	//	_advancedClick();

			//	var username = $(chatMessage.children('.from').get(0)).text();
			//	username = username.substring(0, username.length-2);
				
        	//	$('#privateSpan').val(username + ';');
        	
        	//	_onMessageClose(chatMessage);
        	//});
        	
        	chatMessage.append('<button>'+config.labels.get('offensive')+'</button>');
        	chatMessage.mouseout(function(e){
        		
        		if (!e.relatedTarget)
        			_onMessageClose(chatMessage);
        		
        		if (e.relatedTarget == chatMessage[0] ||
        			e.relatedTarget.parentNode == chatMessage[0])
        			return;
        			
            	_onMessageClose(chatMessage);
            });
            
            last(chatMessage.children()).click( function() {
            	
            	chatMessage.unbind('mouseout');
            	
            	$.alerts.okButton = ' ' + config.labels.get('yes') + ' ';
     			$.alerts.cancelButton = ' '+config.labels.get('no')+' ';
     			       
       			jConfirm(config.labels.get('rushur'),  '', function(r) {
       			
	         		if (r == true) {
                    
	            		//Ok button pressed...
						$.cometd.publish('/service/abuse', {
		                    room: _channel,
		                    user: _username,
		                    message: chatMessage.html(),
		                },
		                { username:config.username, verify:config.verify }
		                );
	         		} 
	         		
	         		_onMessageClose(chatMessage);
       			});
       			
            });
            
            var chat = $('#chat');
            chat[0].scrollTop = chat[0].scrollHeight - chat[0].clientHeight;//chat.outerHeight();
        }
        
        function _onMessageClose(chatMessage)
        {
        	chatMessage.unbind('mouseout');
        	chatMessage.css('background-color','');
        	last(chatMessage.children()).remove();
        	//last(chatMessage.children()).remove();
        	chatMessage.click(function(){
            	_onMessageOpen(chatMessage);
            });
        }

        this.receive = function(message)
        {
            var fromUser = message.data.user;
            var membership = message.data.membership;
            var text = message.data.chat;

			var sameUser = fromUser == _lastUser;

            if (!membership && sameUser)
            {
                fromUser = '...';
            }
            else
            {
                _lastUser = fromUser;
                fromUser += ':';
            }

            var chat = $('#chat');
            if (message.data.user == config.labels.get('system') || membership)
            {
                chat.append('<span class=\"membership\"><span class=\"from\">' + message.data.user + '&nbsp;</span><span class=\"text\">' + text + '</span></span><br/>');
                _lastUser = null;

                if (message.data.user != config.labels.get('system'))
                	_enableInputs();
            }
            //else if (message.data.scope == 'private')
            //{
            //   chat.append('<span class=\"private\"><span class=\"from\">' + fromUser + '&nbsp;</span><span class=\"text\">[private]&nbsp;' + text + '</span></span><br/>');
            //}
            else
            {
            	if (sameUser) {
            		last(chat.children()).append('<br><span class=\"from\">' + fromUser + '&nbsp;</span><span class=\"text\">' + text + '</span>');
            	}
            	else {
                	chat.append('<div class=\"message\"><span class=\"from\">' + fromUser + '&nbsp;</span><span class=\"text\">' + text + '</span></div>');
                	
                	var adiv = last(chat.children());
                	
                	adiv.click(function(){
            			_onMessageOpen(adiv);
            		});
            		//last(chat.children()).mouseout(function(){
            		//	_onMessageClose(adiv);
            		//});
                	
                }
            }

            // There seems to be no easy way in jQuery to handle the scrollTop property
            chat[0].scrollTop = chat[0].scrollHeight - chat[0].clientHeight;//chat.outerHeight();
        };
        
        this.privateReceive = function(message)
        {
        	_lastUser = "";
            var fromUser = message.data.from;
            fromUser += ':';
            var text = message.data.chat;
            var chat = $('#chat');
        	chat.append('<div class=\"message\"><span class="\privateMessage"\>'+config.labels.get('privateMessageFrom')+'</span><span class=\"from privateMessage\">' + fromUser + '&nbsp;</span><span class=\"text\">' + text + '</span></div>');
        	
        	var adiv = last(chat.children());
        	adiv.click(function(){
            	_onMessageOpen(adiv);
            });

            chat[0].scrollTop = chat[0].scrollHeight - chat[0].clientHeight;//chat.outerHeight();
        };

        /**
         * Updates the members list.
         * This function is called when a message arrives on channel /chat/members
         */
        this.members = function(message)
        {
        	_enableInputs();
        
         	var add = true;   
            $.each($('#to').children(), function()
            {
            	if (this.textContent == message.data.user)
            	{
                	add = false;
                }
            });
            
            if (add)
            	$('#to').append('<option>'+message.data.user+'</option>');
        };

		this.changeChannels = function(newChannel)
		{
			if (!newChannel)
				return;
			
			_unsubscribe(); // uses insede the _channel variable!
			
			_channel = "/auth/" + newChannel;
			_channelName = newChannel;

			_subscribe();
		}
		
        function _unsubscribe()
        {
            if (_chatSubscription)
            {
				$.cometd.publish(_channel, {
                    user: _username,
                    membership: 'leave',
                    chat: config.labels.get('leftTheChannel') + _channelName
                },
                { username:config.username, verify:config.verify });

                $.cometd.unsubscribe(_chatSubscription);
            }
            _chatSubscription = null;
            if (_membersSubscription)
            {
                $.cometd.unsubscribe(_membersSubscription);
            }
            _membersSubscription = null;
            
            if (_privateChatSubscription)
            {
                $.cometd.unsubscribe(_privateChatSubscription);
            }
            _privateChatSubscription = null;
            
        }

        function _subscribe()
        {
            _chatSubscription = $.cometd.subscribe(_channel, _self.receive, {username:config.username, verify:config.verify});
            _membersSubscription = $.cometd.subscribe('/auth/chat/members', _self.members, {username:config.username, verify:config.verify});
            _privateChatSubscription = $.cometd.subscribe('/auth/chat/private/'+config.username,
            	_self.privateReceive, {username:config.username, verify:config.verify});
			
			$.cometd.publish(_channel, {
				user: _username,
				membership: 'join',
				chat: config.labels.get('joinedTheChannel') + _channelName
			},
            { username:config.username, verify:config.verify });
        }

        function _connectionInitialized()
        {
            // first time connection for this client, so subscribe tell everybody.
            $.cometd.batch(function()
            {
                _subscribe();
            });
        }
        
        function _initOverlay()
        {
        	 var chat = $("#chat");
			 //var left = chat.offset().left;
			 //var top = chat.offset().top;

			 var width = chat.width();
			 var height = chat.height();

			 //$("#chatOverlay").css("left", left + "px");
			 //$("#chatOverlay").css("top", top + "px");
			 $("#chatOverlay").css("width", width + "px");
			 $("#chatOverlay").css("height", height + "px");

			 $("#chatOverlay").html("<a href='http://www.ligdoltv.com/user/login'>"+config.labels.get('overlayMessage')+"</a>");
        }
        
        function _disableInputs()
        {
        	$('#sendButton')[0].disabled = true;
        	$('#phrase')[0].disabled = true;
        	$('#phrase').unbind('keyup');
        	$("#chatOverlay").css("display", '');
        	//$("#membership").css("opacity", '0.2');
        	//$("#message").css("opacity", '0.2');
        }
        
        function _enableInputs()
        {
        	//$("#membership").css("opacity", '');
        	//$("#message").css("opacity", '');
        	$("#chatOverlay").css('display', 'none');
            $('#sendButton')[0].disabled = false;
        	$('#phrase')[0].disabled = false;
	        $('#phrase').keyup(function(e)
	        {
	            if (e.keyCode == 13)
	            {
	                _self.send();
	            }
	        });
        
        }

        function _connectionEstablished()
        {			
            // connection establish (maybe not for first time), so just
            // tell local user and update membership
            _self.receive({
                data: {
                    user: config.labels.get('system'),
                    chat: config.labels.get('connectionOpened')
                }
            });
            $.cometd.publish('/auth/chat/members', {
                user: _username,
                room: _channel
            },
            { username:config.username, verify:config.verify });
        }

        function _connectionBroken()
        {
        	_disableInputs();
        	
            _self.receive({
                data: {
                    user: config.labels.get('system'),
                    chat: config.labels.get('connectionBroken')
                }
            });
            $('#members').empty();
        }

        function _connectionClosed()
        {
        	_disableInputs();
        	
            _self.receive({
                data: {
                    user: config.labels.get('system'),
                    chat: config.labels.get('connectionClosed')
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
            }
        }

        function _metaHandshake(message)
        {
            if (message.successful)
            {
                _connectionInitialized();
            }
        }

        $.cometd.addListener('/meta/handshake', _metaHandshake);
        $.cometd.addListener('/meta/connect', _metaConnect);

        // Restore the state, if present
        if (state)
        {
            setTimeout(function()
            {
                // This will perform the handshake
                _self.join(state.username);
            }, 0);
        }

        $(window).unload(function()
        {
            if ($.cometd.reload)
            {
                $.cometd.reload();
                // Save the application state only if the user was chatting
                if (_wasConnected && _username)
                {
                    var expires = new Date();
                    expires.setTime(expires.getTime() + 5 * 1000);
                    org.cometd.COOKIE.set('org.cometd.demo.state', org.cometd.JSON.toJSON({
                        username: _username,
                        useServer: $('#useServer').attr('checked'),
                        altServer: $('#altServer').val()
                    }), { 'max-age': 5, expires: expires });
                }
            }
            else
            {
                $.cometd.disconnect();
            }
        });
    }

})(jQuery);
