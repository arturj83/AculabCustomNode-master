//webkitURL is deprecated but nevertheless
URL = window.URL || window.webkitURL;

var cssFileUrl = "%1$s";
var jsFileUrl = "%7$s";
var audioSampleId = "%2$s";
var recHead = "%3$s";
var recFoot = "%4$s";
var recMic = "%5$s";
var recTime = %6$s;

var gumStream; 						//stream from getUserMedia()
var rec; 							    //Recorder.js object
var input; 					  		//MediaStreamAudioSourceNode we'll be recording

var recStarted = false;

// shim for AudioContext when it's not avb.
var AudioContext = window.AudioContext || window.webkitAudioContext;
var audioContext //audio context to help us record

var recordButton;
var hiddenRecording;

//css mic button colors
var backColor;
var backColorActive;


window.startRecording = function () {
	console.log("recordButton clicked");

	/*
		Simple constraints object, for more advanced audio features see
		https://addpipe.com/blog/audio-constraints-getusermedia/
	*/

    var constraints = { audio: true, video:false }

	/*
    	We're using the standard promise based getUserMedia()
    	https://developer.mozilla.org/en-US/docs/Web/API/MediaDevices/getUserMedia
	*/

	var mediaDevices = navigator.mediaDevices;
	if (mediaDevices == null) {
		console.log("Couldn't get the media devices");
		return false;
	}

	//navigator.mediaDevices.getUserMedia(constraints).then(function(stream) {
	mediaDevices.getUserMedia(constraints).then(function(stream) {
		console.log("getUserMedia() success, stream created, initializing Recorder.js ...");

		/*
			create an audio context after getUserMedia is called
			sampleRate might change after getUserMedia is called, like it does on macOS when recording through AirPods
			the sampleRate defaults to the one set in your OS for your playback device

		*/
		audioContext = new AudioContext();

		/*  assign to gumStream for later use  */
		gumStream = stream;

		/* use the stream */
		input = audioContext.createMediaStreamSource(stream);

		/*
			Create the Recorder object and configure to record mono sound (1 channel)
			Recording 2 channels  will double the file size
		*/
		rec = new Recorder(input,{numChannels:1})

		//start the recording process
		rec.record()

		console.log("Recording started");

	}).catch(function(err) {
    	return false;
	});

	return true;

}

window.stopRecording = function () {
	console.log("stopButton clicked");

	//stop the recording
	rec.stop();

	//stop microphone access
	gumStream.getAudioTracks()[0].stop();

	//create the wav blob and pass it on to createDownloadLink
	rec.exportWAV(populateHidden);
	return true;
}

function populateHidden(blob) {

	blobToBase64(blob).then(res => {
		document.getElementById(audioSampleId).value = res;
	});
}

const blobToBase64 = blob => {
  const reader = new FileReader();
  reader.readAsDataURL(blob);
  return new Promise(resolve => {
    reader.onloadend = () => {
			var dataUrl = reader.result;
			var base64  = dataUrl.split(',')[1];
      resolve(base64);
    };
  });
};

window.pressRecording = function () {

	if (recStarted == false) {
		  recStarted = startRecording();
			var animation = {};
			animation.backgroundColor = backColorActive;
			if (recTime > 0) {
				animation.opacity = 0.2;
			}
			if (recStarted == true) {
				$('#circlein').animate(animation, 300);
				if (recTime > 0) {
					recordButton.style.pointerEvents = "none";
					setTimeout(function() { pressRecording(); }, recTime);
				}
			}

			return;

	}

	if (recStarted == true) {

			recStarted = stopRecording();
			var backColor = document.getElementById('circlein').style.backgroundColor;
			var animation = {};
			animation.backgroundColor = backColor;
			if (recTime > 0) {
				animation.opacity = 1.0;
			}
			if (recStarted == true) {
				$('#circlein').animate(animation, 300, function () {
					if (recTime > 0) {
						recordButton.style.pointerEvents = "auto";
					}
					recStarted = false;
					submitRecording();
				});
			}
			return;
	}

}

window.submitRecording = function () {
		document.getElementById("loginButton_0").click();
}


function setRecording (hidden_field_id) {

	// shim for AudioContext when it's not avb.
	AudioContext = window.AudioContext || window.webkitAudioContext;
	audioContext //audio context to help us record

	//recordButton    = document.getElementById("acuMicButton");
	recordButton    = document.getElementById("acuDivMic");

	//recordButton.addEventListener('click', function() {
	//		pressRecording();
	//});
	recordButton.addEventListener('click', pressRecording);

}


function generateLayout(hidden_field_id, head_contents, foot_contents) {

  var acu_main_layout = "<div id='aculab_header' class='acuDivHeader'>" + head_contents + "</div>";
  acu_main_layout    += "<div id='aculab_recorder' class='acuDivRecorder'>";
  acu_main_layout    += "<div id='acuDivMic' class='acuDivMic'>" + recMic + "</div>";
  acu_main_layout    += "</div>";
  acu_main_layout    += "<div id='aculab_download' class='acuDivDownload'><ol id='recordingsList'></ol></div>";
  acu_main_layout    += "<div id='aculab_footer' class='acuDivFooter'>" + foot_contents + "</div>";
	acu_main_layout    += "<div id='circlein_active' class='circlein_active'></div>";

  return acu_main_layout;

}

function setAculabLogo() {
    var div_aculab_logo = document.getElementById('aculab_logo');
    if (div_aculab_logo != null) {
        div_aculab_logo.innerHTML = "<img id='aculab_logo_img' src='https:\/\/cdn.contactcenterworld.com/images/company/aculab-1200px-logo.png' width='150px' height='28px' class='acuImgLogo' alt='aculab'>";
    }
}


function setCSSFile (cssFile) {

		if (cssFile == null)
				return;

		if (cssFile === "")
				return;

    var style = document.createElement('link');
    style.type = 'text/css';
    style.rel  = 'stylesheet';
    style.href = cssFile;
    document.getElementsByTagName('head')[0].appendChild(style);

}

function setJsFile (jsFile) {
    var script = document.createElement('script');
    script.src = jsFile;
    document.getElementsByTagName('head')[0].appendChild(script);

}

function runTheScript (css_file_url, js_file_url, hidden_field_id, acu_header, acu_footer) {

    var loginButton = document.getElementById("loginButton_0");
    if (loginButton != null) {
        loginButton.style.display="none";
    }

    var callback_1= document.getElementById("callback_1");
    if (callback_1 != null) {
        callback_1.innerHTML = generateLayout(hidden_field_id, acu_header, acu_footer);

        setCSSFile(css_file_url);
				//setJsFile('https://openam.partner.com:8443/js/recorder.js');
				setJsFile('https://code.jquery.com/jquery-1.12.4.js');
				setJsFile('https://code.jquery.com/ui/1.12.1/jquery-ui.js');

        setRecording(hidden_field_id);

				backColor       = window.getComputedStyle(document.getElementById('circlein')).getPropertyValue("background-color");
				backColorActive = window.getComputedStyle(document.getElementById('circlein_active')).getPropertyValue("background-color");

				if (js_file_url.length != 0) {
					var customJs = document.createElement('script');
					customJs.src = js_file_url;
					callback_1.parentNode.appendChild(customJs);
				}

    }

}

runTheScript (cssFileUrl, jsFileUrl, audioSampleId, recHead, recFoot);
