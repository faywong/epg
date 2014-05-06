$(function() {
	/**
	 * 时间轴fixed效果
	 */
	(function() {
/*		if ($.browser.msie && $.browser.version <= 6)
			return false;*/
		var $window = $(window), layerStickZW, layerStick = $('#layer_head'), marginTop = layerStick
				.css('margin-top'), marginBottom = layerStick
				.css('margin-bottom'), layerStickTop = layerStick.offset().top, layerStickHeight = layerStick
				.outerHeight();
/*		$window.scroll(function() {
			if ($window.scrollTop() > layerStickTop) {
				if (!layerStick.data('fixed')) {
					layerStickZW = $("<div></div>").css({
						height : layerStickHeight,
						"margin-top" : marginTop,
						"margin-bottom" : marginBottom
					}).insertBefore(layerStick);
					layerStick.css({
						"position" : "fixed",
						"left" : "50%",
						"margin" : "0 0 0 -470px",
						"top" : 0,
						"z-index" : 199
					});
					layerStick.data('fixed', 1);
					$('#epg-sift-bd-download').show();
					$('#es-right').css('padding-top', '10px');
				}
			} else {
				layerStickZW && layerStickZW.remove();
				layerStick.css({
					"position" : "static",
					"left" : "auto",
					"top" : "auto",
					"z-index" : 0,
					"margin" : "0 auto"
				});
				layerStick.removeData('fixed');
				$('#epg-sift-bd-download').hide();
				$('#es-right').css('padding-top', '10px');
			}
		})*/
	})();
});

/*$(function(){
 (function(){
 if($.browser.msie&&$.browser.msie.version<=6){
 return false
 }
 var g=$(window),a,f=$("#layer_head"),c=f.css("margin-top"),e=f.css("margin-bottom"),d=f.offset().top,b=f.outerHeight();
 g.scroll(function(){
 if(g.scrollTop()>d){
 if(!f.data("fixed")){
 a=$("<div></div>").css({height:b,"margin-top":c,"margin-bottom":e}).insertBefore(f);
 f.css({position:"fixed",left:"50%",margin:"0 0 0 -470px",top:0,"z-index":9999});
 f.data("fixed",1)
 }
 }else{
 a&&a.remove();
 f.css({position:"static",left:"auto",top:"auto","z-index":0,margin:"0 auto"});
 f.removeData("fixed")
 }
 })
 })()
 });*/

/*document.onkeydown = function (e) {
 var ev = window.event || e;
 var code = ev.keyCode || ev.which;
 if (code == 116) {
 $('#CURRENT_DAY_BTN').triggerHandler("click");
 ev.keyCode ? ev.keyCode = 0 : ev.which = 0;
 cancelBubble = true;
 return false;
 }
 }

 document.oncontextmenu = function(){
 return   false;
 }*/