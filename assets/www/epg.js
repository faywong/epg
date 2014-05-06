
/**
 *
 * epg.js
 *
 * this script holds the main epg related interactivity & UI dynamically layout logics:
 *
 * 1) load channel, epg event, content type data, invoke functions implemented in Java
 * 2) populate & update the UI accordingly
 * 3) handle the user input(navigation through keyboard, mouse click, touch, etc)
 */

jQuery.fn.extend({
	closestToOffset: function(offset) {
	    var target = null, elOffset, x = offset.left, y = offset.top, distance, dx, dy, minDistance;
	    //console.log("closestToOffset() in, direction:" + (offset.rightDirection ? "Right" : "Left") + " left:" + offset.left);
	    this.each(function() {
	        elOffset = $(this).offset();
	        elOffset.right = elOffset.left + $(this).outerWidth();
	        elOffset.bottom = elOffset.top + $(this).outerHeight();

	        //console.log("child element[title:" + $(this).attr("title") + " left:" + elOffset.left  + " top:" + elOffset.top + " right:" + elOffset.right + " bottom:" + elOffset.bottom +"]")
	        if (offset.rightDirection) {
	        	distance = elOffset.left - x;
	        	if (distance <= 0) {
	        		distance = Math.abs(distance);
		            if ((minDistance === undefined || distance < minDistance)) {
		                minDistance = distance;
		                target = $(this);
		            }
	        	}
	        } else {
	        	if (elOffset.right >= x && elOffset.left < x) {
	        		console.log("best case");
	        		target = $(this);
	        		// don't continue the each loop
	        		return false;
	        	}
	        	distance = elOffset.right - x;
        		distance = Math.abs(distance);
	            if ((minDistance === undefined || distance < minDistance)) {
	                minDistance = distance;
	                target = $(this);
	            }
	        }
	    });
	    //console.log("closestToOffset() out with title:" + (target == null ? "null object" : target.attr("title")));
	    return target;
	}
});

/*
 * fake EPGUtils for debugging in PC browsers
 * if you run this webpage in Android WebView, please comment it.
 */

/*
var EPGUtils = {
	getEPG : function (start, duration) {
		return '{"10":[{"title":"打狗棍  19","channel_num":"10","start_time":1392717517,"end_time":1392719677,"playing":0,"play_range":"9:58:37 AM-10:34:37 AM","duration":2160,"short_desc":"打狗棍第19集，剧情...","detail_desc":"二丫头希望两个手下能提供让男性充满情欲的酒水，两个手下会过意来，立即找来了酒水给二丫头饮用。深夜，二丫头与素芝睡在床上，虽然喝了药酒，但他依然没有发现命根有动静，情急之下他从床上坐了起来，无可奈何看着素芝，素芝深明大理，劝说二丫头不要急燥。","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"打狗棍  20","channel_num":"10","start_time":1392719677,"end_time":1392722337,"playing":0,"play_range":"10:34:37 AM-11:18:57 AM","duration":2660,"short_desc":"打狗棍第20集，剧情...","detail_desc":"打狗棍第20集，详细剧情...","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"爸爸去哪儿","channel_num":"10","start_time":1392722337,"end_time":1392725137,"playing":0,"play_range":"11:18:57 AM-12:05:37 PM","duration":2800,"short_desc":"爸爸去哪儿，张亮让天天追森蝶...","detail_desc":"爸爸去哪儿，张亮让天天追森蝶，详细剧情...","pinyin_keyword":"BA BA QU NA ER","content_category":"娱乐","thumbnail_url":"images/bbqne.jpg"},{"title":"天天向上","channel_num":"10","start_time":1392725137,"end_time":1392727437,"playing":1,"play_range":"12:05:37 PM-12:43:57 PM","duration":2300,"short_desc":"天天向上，广告大伽...","detail_desc":"天天向上，广告大伽，详细剧情...","pinyin_keyword":"TIAN TIAN XIANG SHANG","content_category":"娱乐","thumbnail_url":"images/ttxs.jpg"},{"title":"非常了得","channel_num":"10","start_time":1392727437,"end_time":1392734637,"playing":0,"play_range":"12:43:57 PM-2:43:57 PM","duration":7200,"short_desc":"非常了得，郭德纲被美女锁喉...","detail_desc":"非常了得，郭德纲被美女锁喉，详细剧情...","pinyin_keyword":"FEI CHANG LE DE","content_category":"娱乐","thumbnail_url":"images/fcld.jpg"},{"title":"新闻联播","channel_num":"10","start_time":1392734637,"end_time":1392738237,"playing":0,"play_range":"2:43:57 PM-3:43:57 PM","duration":3600,"short_desc":"习近平吃包子...","detail_desc":"新闻联播，习近平吃包子，详细剧情...","pinyin_keyword":"XIN WEN LIAN BO","content_category":"新闻","thumbnail_url":"images/xwlb.jpg"},{"title":"NULL Event","channel_num":"10","start_time":1392738237,"end_time":1392741837,"playing":0,"play_range":"3:43:57 PM-4:43:57 PM","duration":3600,"short_desc":"NULL Event","detail_desc":"NULL Event","pinyin_keyword":"","content_category":"NULL","thumbnail_url":"images/xwlb.jpg"}],"7":[{"title":"打狗棍  19","channel_num":"7","start_time":1392717517,"end_time":1392719677,"playing":0,"play_range":"9:58:37 AM-10:34:37 AM","duration":2160,"short_desc":"打狗棍第19集，剧情...","detail_desc":"二丫头希望两个手下能提供让男性充满情欲的酒水，两个手下会过意来，立即找来了酒水给二丫头饮用。深夜，二丫头与素芝睡在床上，虽然喝了药酒，但他依然没有发现命根有动静，情急之下他从床上坐了起来，无可奈何看着素芝，素芝深明大理，劝说二丫头不要急燥。","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"打狗棍  20","channel_num":"7","start_time":1392719677,"end_time":1392722337,"playing":0,"play_range":"10:34:37 AM-11:18:57 AM","duration":2660,"short_desc":"打狗棍第20集，剧情...","detail_desc":"打狗棍第20集，详细剧情...","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"爸爸去哪儿","channel_num":"7","start_time":1392722337,"end_time":1392725137,"playing":0,"play_range":"11:18:57 AM-12:05:37 PM","duration":2800,"short_desc":"爸爸去哪儿，张亮让天天追森蝶...","detail_desc":"爸爸去哪儿，张亮让天天追森蝶，详细剧情...","pinyin_keyword":"BA BA QU NA ER","content_category":"娱乐","thumbnail_url":"images/bbqne.jpg"},{"title":"天天向上","channel_num":"7","start_time":1392725137,"end_time":1392727437,"playing":1,"play_range":"12:05:37 PM-12:43:57 PM","duration":2300,"short_desc":"天天向上，广告大伽...","detail_desc":"天天向上，广告大伽，详细剧情...","pinyin_keyword":"TIAN TIAN XIANG SHANG","content_category":"娱乐","thumbnail_url":"images/ttxs.jpg"},{"title":"非常了得","channel_num":"7","start_time":1392727437,"end_time":1392734637,"playing":0,"play_range":"12:43:57 PM-2:43:57 PM","duration":7200,"short_desc":"非常了得，郭德纲被美女锁喉...","detail_desc":"非常了得，郭德纲被美女锁喉，详细剧情...","pinyin_keyword":"FEI CHANG LE DE","content_category":"娱乐","thumbnail_url":"images/fcld.jpg"},{"title":"新闻联播","channel_num":"7","start_time":1392734637,"end_time":1392738237,"playing":0,"play_range":"2:43:57 PM-3:43:57 PM","duration":3600,"short_desc":"习近平吃包子...","detail_desc":"新闻联播，习近平吃包子，详细剧情...","pinyin_keyword":"XIN WEN LIAN BO","content_category":"新闻","thumbnail_url":"images/xwlb.jpg"},{"title":"NULL Event","channel_num":"7","start_time":1392738237,"end_time":1392741837,"playing":0,"play_range":"3:43:57 PM-4:43:57 PM","duration":3600,"short_desc":"NULL Event","detail_desc":"NULL Event","pinyin_keyword":"","content_category":"NULL","thumbnail_url":"images/xwlb.jpg"}],"9":[{"title":"打狗棍  19","channel_num":"9","start_time":1392717517,"end_time":1392719677,"playing":0,"play_range":"9:58:37 AM-10:34:37 AM","duration":2160,"short_desc":"打狗棍第19集，剧情...","detail_desc":"二丫头希望两个手下能提供让男性充满情欲的酒水，两个手下会过意来，立即找来了酒水给二丫头饮用。深夜，二丫头与素芝睡在床上，虽然喝了药酒，但他依然没有发现命根有动静，情急之下他从床上坐了起来，无可奈何看着素芝，素芝深明大理，劝说二丫头不要急燥。","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"打狗棍  20","channel_num":"9","start_time":1392719677,"end_time":1392722337,"playing":0,"play_range":"10:34:37 AM-11:18:57 AM","duration":2660,"short_desc":"打狗棍第20集，剧情...","detail_desc":"打狗棍第20集，详细剧情...","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"爸爸去哪儿","channel_num":"9","start_time":1392722337,"end_time":1392725137,"playing":0,"play_range":"11:18:57 AM-12:05:37 PM","duration":2800,"short_desc":"爸爸去哪儿，张亮让天天追森蝶...","detail_desc":"爸爸去哪儿，张亮让天天追森蝶，详细剧情...","pinyin_keyword":"BA BA QU NA ER","content_category":"娱乐","thumbnail_url":"images/bbqne.jpg"},{"title":"天天向上","channel_num":"9","start_time":1392725137,"end_time":1392727437,"playing":1,"play_range":"12:05:37 PM-12:43:57 PM","duration":2300,"short_desc":"天天向上，广告大伽...","detail_desc":"天天向上，广告大伽，详细剧情...","pinyin_keyword":"TIAN TIAN XIANG SHANG","content_category":"娱乐","thumbnail_url":"images/ttxs.jpg"},{"title":"非常了得","channel_num":"9","start_time":1392727437,"end_time":1392734637,"playing":0,"play_range":"12:43:57 PM-2:43:57 PM","duration":7200,"short_desc":"非常了得，郭德纲被美女锁喉...","detail_desc":"非常了得，郭德纲被美女锁喉，详细剧情...","pinyin_keyword":"FEI CHANG LE DE","content_category":"娱乐","thumbnail_url":"images/fcld.jpg"},{"title":"新闻联播","channel_num":"9","start_time":1392734637,"end_time":1392738237,"playing":0,"play_range":"2:43:57 PM-3:43:57 PM","duration":3600,"short_desc":"习近平吃包子...","detail_desc":"新闻联播，习近平吃包子，详细剧情...","pinyin_keyword":"XIN WEN LIAN BO","content_category":"新闻","thumbnail_url":"images/xwlb.jpg"},{"title":"NULL Event","channel_num":"9","start_time":1392738237,"end_time":1392741837,"playing":0,"play_range":"3:43:57 PM-4:43:57 PM","duration":3600,"short_desc":"NULL Event","detail_desc":"NULL Event","pinyin_keyword":"","content_category":"NULL","thumbnail_url":"images/xwlb.jpg"}],"8":[{"title":"打狗棍  19","channel_num":"8","start_time":1392717517,"end_time":1392719677,"playing":0,"play_range":"9:58:37 AM-10:34:37 AM","duration":2160,"short_desc":"打狗棍第19集，剧情...","detail_desc":"二丫头希望两个手下能提供让男性充满情欲的酒水，两个手下会过意来，立即找来了酒水给二丫头饮用。深夜，二丫头与素芝睡在床上，虽然喝了药酒，但他依然没有发现命根有动静，情急之下他从床上坐了起来，无可奈何看着素芝，素芝深明大理，劝说二丫头不要急燥。","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"打狗棍  20","channel_num":"8","start_time":1392719677,"end_time":1392722337,"playing":0,"play_range":"10:34:37 AM-11:18:57 AM","duration":2660,"short_desc":"打狗棍第20集，剧情...","detail_desc":"打狗棍第20集，详细剧情...","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"爸爸去哪儿","channel_num":"8","start_time":1392722337,"end_time":1392725137,"playing":0,"play_range":"11:18:57 AM-12:05:37 PM","duration":2800,"short_desc":"爸爸去哪儿，张亮让天天追森蝶...","detail_desc":"爸爸去哪儿，张亮让天天追森蝶，详细剧情...","pinyin_keyword":"BA BA QU NA ER","content_category":"娱乐","thumbnail_url":"images/bbqne.jpg"},{"title":"天天向上","channel_num":"8","start_time":1392725137,"end_time":1392727437,"playing":1,"play_range":"12:05:37 PM-12:43:57 PM","duration":2300,"short_desc":"天天向上，广告大伽...","detail_desc":"天天向上，广告大伽，详细剧情...","pinyin_keyword":"TIAN TIAN XIANG SHANG","content_category":"娱乐","thumbnail_url":"images/ttxs.jpg"},{"title":"非常了得","channel_num":"8","start_time":1392727437,"end_time":1392734637,"playing":0,"play_range":"12:43:57 PM-2:43:57 PM","duration":7200,"short_desc":"非常了得，郭德纲被美女锁喉...","detail_desc":"非常了得，郭德纲被美女锁喉，详细剧情...","pinyin_keyword":"FEI CHANG LE DE","content_category":"娱乐","thumbnail_url":"images/fcld.jpg"},{"title":"新闻联播","channel_num":"8","start_time":1392734637,"end_time":1392738237,"playing":0,"play_range":"2:43:57 PM-3:43:57 PM","duration":3600,"short_desc":"习近平吃包子...","detail_desc":"新闻联播，习近平吃包子，详细剧情...","pinyin_keyword":"XIN WEN LIAN BO","content_category":"新闻","thumbnail_url":"images/xwlb.jpg"},{"title":"NULL Event","channel_num":"8","start_time":1392738237,"end_time":1392741837,"playing":0,"play_range":"3:43:57 PM-4:43:57 PM","duration":3600,"short_desc":"NULL Event","detail_desc":"NULL Event","pinyin_keyword":"","content_category":"NULL","thumbnail_url":"images/xwlb.jpg"}],"13":[{"title":"打狗棍  19","channel_num":"13","start_time":1392717518,"end_time":1392719678,"playing":0,"play_range":"9:58:38 AM-10:34:38 AM","duration":2160,"short_desc":"打狗棍第19集，剧情...","detail_desc":"二丫头希望两个手下能提供让男性充满情欲的酒水，两个手下会过意来，立即找来了酒水给二丫头饮用。深夜，二丫头与素芝睡在床上，虽然喝了药酒，但他依然没有发现命根有动静，情急之下他从床上坐了起来，无可奈何看着素芝，素芝深明大理，劝说二丫头不要急燥。","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"打狗棍  20","channel_num":"13","start_time":1392719678,"end_time":1392722338,"playing":0,"play_range":"10:34:38 AM-11:18:58 AM","duration":2660,"short_desc":"打狗棍第20集，剧情...","detail_desc":"打狗棍第20集，详细剧情...","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"爸爸去哪儿","channel_num":"13","start_time":1392722338,"end_time":1392725138,"playing":0,"play_range":"11:18:58 AM-12:05:38 PM","duration":2800,"short_desc":"爸爸去哪儿，张亮让天天追森蝶...","detail_desc":"爸爸去哪儿，张亮让天天追森蝶，详细剧情...","pinyin_keyword":"BA BA QU NA ER","content_category":"娱乐","thumbnail_url":"images/bbqne.jpg"},{"title":"天天向上","channel_num":"13","start_time":1392725138,"end_time":1392727438,"playing":1,"play_range":"12:05:38 PM-12:43:58 PM","duration":2300,"short_desc":"天天向上，广告大伽...","detail_desc":"天天向上，广告大伽，详细剧情...","pinyin_keyword":"TIAN TIAN XIANG SHANG","content_category":"娱乐","thumbnail_url":"images/ttxs.jpg"},{"title":"非常了得","channel_num":"13","start_time":1392727438,"end_time":1392734638,"playing":0,"play_range":"12:43:58 PM-2:43:58 PM","duration":7200,"short_desc":"非常了得，郭德纲被美女锁喉...","detail_desc":"非常了得，郭德纲被美女锁喉，详细剧情...","pinyin_keyword":"FEI CHANG LE DE","content_category":"娱乐","thumbnail_url":"images/fcld.jpg"},{"title":"新闻联播","channel_num":"13","start_time":1392734638,"end_time":1392738238,"playing":0,"play_range":"2:43:58 PM-3:43:58 PM","duration":3600,"short_desc":"习近平吃包子...","detail_desc":"新闻联播，习近平吃包子，详细剧情...","pinyin_keyword":"XIN WEN LIAN BO","content_category":"新闻","thumbnail_url":"images/xwlb.jpg"},{"title":"NULL Event","channel_num":"13","start_time":1392738238,"end_time":1392741837,"playing":0,"play_range":"3:43:58 PM-4:43:57 PM","duration":3599,"short_desc":"NULL Event","detail_desc":"NULL Event","pinyin_keyword":"","content_category":"NULL","thumbnail_url":"images/xwlb.jpg"}],"14":[{"title":"打狗棍  19","channel_num":"14","start_time":1392717518,"end_time":1392719678,"playing":0,"play_range":"9:58:38 AM-10:34:38 AM","duration":2160,"short_desc":"打狗棍第19集，剧情...","detail_desc":"二丫头希望两个手下能提供让男性充满情欲的酒水，两个手下会过意来，立即找来了酒水给二丫头饮用。深夜，二丫头与素芝睡在床上，虽然喝了药酒，但他依然没有发现命根有动静，情急之下他从床上坐了起来，无可奈何看着素芝，素芝深明大理，劝说二丫头不要急燥。","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"打狗棍  20","channel_num":"14","start_time":1392719678,"end_time":1392722338,"playing":0,"play_range":"10:34:38 AM-11:18:58 AM","duration":2660,"short_desc":"打狗棍第20集，剧情...","detail_desc":"打狗棍第20集，详细剧情...","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"爸爸去哪儿","channel_num":"14","start_time":1392722338,"end_time":1392725138,"playing":0,"play_range":"11:18:58 AM-12:05:38 PM","duration":2800,"short_desc":"爸爸去哪儿，张亮让天天追森蝶...","detail_desc":"爸爸去哪儿，张亮让天天追森蝶，详细剧情...","pinyin_keyword":"BA BA QU NA ER","content_category":"娱乐","thumbnail_url":"images/bbqne.jpg"},{"title":"天天向上","channel_num":"14","start_time":1392725138,"end_time":1392727438,"playing":1,"play_range":"12:05:38 PM-12:43:58 PM","duration":2300,"short_desc":"天天向上，广告大伽...","detail_desc":"天天向上，广告大伽，详细剧情...","pinyin_keyword":"TIAN TIAN XIANG SHANG","content_category":"娱乐","thumbnail_url":"images/ttxs.jpg"},{"title":"非常了得","channel_num":"14","start_time":1392727438,"end_time":1392734638,"playing":0,"play_range":"12:43:58 PM-2:43:58 PM","duration":7200,"short_desc":"非常了得，郭德纲被美女锁喉...","detail_desc":"非常了得，郭德纲被美女锁喉，详细剧情...","pinyin_keyword":"FEI CHANG LE DE","content_category":"娱乐","thumbnail_url":"images/fcld.jpg"},{"title":"新闻联播","channel_num":"14","start_time":1392734638,"end_time":1392738238,"playing":0,"play_range":"2:43:58 PM-3:43:58 PM","duration":3600,"short_desc":"习近平吃包子...","detail_desc":"新闻联播，习近平吃包子，详细剧情...","pinyin_keyword":"XIN WEN LIAN BO","content_category":"新闻","thumbnail_url":"images/xwlb.jpg"},{"title":"NULL Event","channel_num":"14","start_time":1392738238,"end_time":1392741837,"playing":0,"play_range":"3:43:58 PM-4:43:57 PM","duration":3599,"short_desc":"NULL Event","detail_desc":"NULL Event","pinyin_keyword":"","content_category":"NULL","thumbnail_url":"images/xwlb.jpg"}],"11":[{"title":"打狗棍  19","channel_num":"11","start_time":1392717517,"end_time":1392719677,"playing":0,"play_range":"9:58:37 AM-10:34:37 AM","duration":2160,"short_desc":"打狗棍第19集，剧情...","detail_desc":"二丫头希望两个手下能提供让男性充满情欲的酒水，两个手下会过意来，立即找来了酒水给二丫头饮用。深夜，二丫头与素芝睡在床上，虽然喝了药酒，但他依然没有发现命根有动静，情急之下他从床上坐了起来，无可奈何看着素芝，素芝深明大理，劝说二丫头不要急燥。","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"打狗棍  20","channel_num":"11","start_time":1392719677,"end_time":1392722337,"playing":0,"play_range":"10:34:37 AM-11:18:57 AM","duration":2660,"short_desc":"打狗棍第20集，剧情...","detail_desc":"打狗棍第20集，详细剧情...","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"爸爸去哪儿","channel_num":"11","start_time":1392722337,"end_time":1392725137,"playing":0,"play_range":"11:18:57 AM-12:05:37 PM","duration":2800,"short_desc":"爸爸去哪儿，张亮让天天追森蝶...","detail_desc":"爸爸去哪儿，张亮让天天追森蝶，详细剧情...","pinyin_keyword":"BA BA QU NA ER","content_category":"娱乐","thumbnail_url":"images/bbqne.jpg"},{"title":"天天向上","channel_num":"11","start_time":1392725137,"end_time":1392727437,"playing":1,"play_range":"12:05:37 PM-12:43:57 PM","duration":2300,"short_desc":"天天向上，广告大伽...","detail_desc":"天天向上，广告大伽，详细剧情...","pinyin_keyword":"TIAN TIAN XIANG SHANG","content_category":"娱乐","thumbnail_url":"images/ttxs.jpg"},{"title":"非常了得","channel_num":"11","start_time":1392727437,"end_time":1392734637,"playing":0,"play_range":"12:43:57 PM-2:43:57 PM","duration":7200,"short_desc":"非常了得，郭德纲被美女锁喉...","detail_desc":"非常了得，郭德纲被美女锁喉，详细剧情...","pinyin_keyword":"FEI CHANG LE DE","content_category":"娱乐","thumbnail_url":"images/fcld.jpg"},{"title":"新闻联播","channel_num":"11","start_time":1392734637,"end_time":1392738237,"playing":0,"play_range":"2:43:57 PM-3:43:57 PM","duration":3600,"short_desc":"习近平吃包子...","detail_desc":"新闻联播，习近平吃包子，详细剧情...","pinyin_keyword":"XIN WEN LIAN BO","content_category":"新闻","thumbnail_url":"images/xwlb.jpg"},{"title":"晚间新闻","channel_num":"11","start_time":1392738237,"end_time":1392741837,"playing":0,"play_range":"3:43:57 PM-4:43:57 PM","duration":3600,"short_desc":"习近平不吃包子啦...","detail_desc":"晚间新闻，习近平不吃包子，改吃什么呢？详细剧情...","pinyin_keyword":"WAN JIAN XIN WEN","content_category":"新闻","thumbnail_url":"images/xwlb.jpg"}],"12":[{"title":"打狗棍  19","channel_num":"12","start_time":1392717518,"end_time":1392719678,"playing":0,"play_range":"9:58:38 AM-10:34:38 AM","duration":2160,"short_desc":"打狗棍第19集，剧情...","detail_desc":"二丫头希望两个手下能提供让男性充满情欲的酒水，两个手下会过意来，立即找来了酒水给二丫头饮用。深夜，二丫头与素芝睡在床上，虽然喝了药酒，但他依然没有发现命根有动静，情急之下他从床上坐了起来，无可奈何看着素芝，素芝深明大理，劝说二丫头不要急燥。","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"打狗棍  20","channel_num":"12","start_time":1392719678,"end_time":1392722338,"playing":0,"play_range":"10:34:38 AM-11:18:58 AM","duration":2660,"short_desc":"打狗棍第20集，剧情...","detail_desc":"打狗棍第20集，详细剧情...","pinyin_keyword":"DA GOU GUN","content_category":"电视剧","thumbnail_url":"images/dgg.jpg"},{"title":"爸爸去哪儿","channel_num":"12","start_time":1392722338,"end_time":1392725138,"playing":0,"play_range":"11:18:58 AM-12:05:38 PM","duration":2800,"short_desc":"爸爸去哪儿，张亮让天天追森蝶...","detail_desc":"爸爸去哪儿，张亮让天天追森蝶，详细剧情...","pinyin_keyword":"BA BA QU NA ER","content_category":"娱乐","thumbnail_url":"images/bbqne.jpg"},{"title":"天天向上","channel_num":"12","start_time":1392725138,"end_time":1392727438,"playing":1,"play_range":"12:05:38 PM-12:43:58 PM","duration":2300,"short_desc":"天天向上，广告大伽...","detail_desc":"天天向上，广告大伽，详细剧情...","pinyin_keyword":"TIAN TIAN XIANG SHANG","content_category":"娱乐","thumbnail_url":"images/ttxs.jpg"},{"title":"非常了得","channel_num":"12","start_time":1392727438,"end_time":1392734638,"playing":0,"play_range":"12:43:58 PM-2:43:58 PM","duration":7200,"short_desc":"非常了得，郭德纲被美女锁喉...","detail_desc":"非常了得，郭德纲被美女锁喉，详细剧情...","pinyin_keyword":"FEI CHANG LE DE","content_category":"娱乐","thumbnail_url":"images/fcld.jpg"},{"title":"新闻联播","channel_num":"12","start_time":1392734638,"end_time":1392738238,"playing":0,"play_range":"2:43:58 PM-3:43:58 PM","duration":3600,"short_desc":"习近平吃包子...","detail_desc":"新闻联播，习近平吃包子，详细剧情...","pinyin_keyword":"XIN WEN LIAN BO","content_category":"新闻","thumbnail_url":"images/xwlb.jpg"},{"title":"NULL Event","channel_num":"12","start_time":1392738238,"end_time":1392741837,"playing":0,"play_range":"3:43:58 PM-4:43:57 PM","duration":3599,"short_desc":"NULL Event","detail_desc":"NULL Event","pinyin_keyword":"","content_category":"NULL","thumbnail_url":"images/xwlb.jpg"}]}';
	},
	getChannelList : function () {
		return '[{"Id":"7","name":"陕西卫视","service_id":"7","logo_url":"images/channel/陕西卫视65.jpg","pinyin_keyword":"SHAN XI WEI SHI"},{"Id":"8","name":"广东卫视","service_id":"8","logo_url":"images/channel/广东卫视65_50.jpg","pinyin_keyword":"GUANG DONG WEI SHI"},{"Id":"9","name":"江苏靓妆","service_id":"9","logo_url":"images/channel/JSTV.gif","pinyin_keyword":"JIANG SU JING ZHUANG"},{"Id":"10","name":"河北卫视","service_id":"10","logo_url":"images/channel/HEBEI.gif","pinyin_keyword":"HE BEI WEI SHI"},{"Id":"11","name":"江西卫视","service_id":"11","logo_url":"images/channel/江西卫视65.jpg","pinyin_keyword":"JIANG XI WEI SHI"},{"Id":"12","name":"东南卫视","service_id":"12","logo_url":"images/channel/FJTV.gif","pinyin_keyword":"DONG NAN WEI SHI"},{"Id":"13","name":"江西卫视","service_id":"13","logo_url":"images/channel/江西卫视65.jpg","pinyin_keyword":"JIANG XI WEI SHI"},{"Id":"14","name":"山东卫视","service_id":"14","logo_url":"images/channel/SDTV.gif","pinyin_keyword":"SHAN DONG WEI SHI"}]';
	},
	getContentTypeList : function() {
		return '["全部", "新闻","娱乐","电视剧"]';
	},
	remind : function () {
		//console.log('fake remind called!');s
	},
	record : function () {
		//console.log('fake record called!');
	},
	playback : function (channel) {
		//console.log('fake playback(' + channel + ') called!');
	}
}
*/

/**
 * global variables
 */
const NULLEventTitle = "NULL Event";
var MRVLEpg = null;
var channelList = null;
var epgEvents = null;
var currentChannelPage = 0; // the current page of channel(switching through up & down focus navigate)
var currentFocusedEvent = {
		title : '',
		channel : '',
		start : '',
		end : ''
}

/**
 * common utils
 */

/**
 * format a number to fixed-length string, prefix with 0 if needed
 *
 * parameter:
 * Source
 *        the string to be formatted
 * Length
 *        the length of string to be returned
 */
function FormatNum(Source,Length){
	var strTemp	=	'';
	for(i=1; i <= Length - String(Source).length; i++){
		strTemp	+=	"0";
	}
	return strTemp+Source;
}

function currentDate() {
	return new Date();
}

var syncCurrentTime = function() {
	var date = currentDate();
	$("span.current-time").text('当前时间：'+ date.getHours()+':'+FormatNum(date.getMinutes(), 2));
};

var initEpgDescPanel = function() {
	var handler_in = function(e) {
	    var dpane      = $('#details-panel');
	    var dpanel_title = $('#details-panel .title');
	    var dpanel_desc  = $('#details-panel .desc');
	    var dpanel_playtime  = $('#details-panel .playtime');
	    var event_title   = $(this).attr('title');
	    var href = $(this).find('div.tp a').attr('href');
	    var newdesc    = $(this).attr('detail_desc');
	    var thumbnail_url     = $(this).attr('thumbnail_url');

	    var channel_num = $(this).attr('channel_num');
	    var play_range = $(this).attr('play_range');
	    var start_time = $(this).attr('start_time');
	    var end_time = $(this).attr('end_time');
	    var linkurl    = $(this).parent().attr('href');

	    currentFocusedEvent.title = event_title;
	    currentFocusedEvent.channel = channel_num;
	    currentFocusedEvent.start = start_time;
	    currentFocusedEvent.end = end_time;

	    var offset = $(this).offset();
	    var program_width = $(this).outerWidth(true);
	    var program_height = $(this).outerHeight(true);
	    var detail_panel_width = $('#details-panel').outerWidth();
	    var detail_panel_height = $('#details-panel').outerHeight();
	    var leftspace = offset.left;
	    var rightspace = ($(window).width() - (offset.left + program_width));
	    if (leftspace >= rightspace) {
	    	var xcoord = Math.max(offset.left - detail_panel_width, 0);
	    } else {
	    	var xcoord = Math.min(offset.left + program_width, $(window).width() - detail_panel_width);
	    }
	    var topspace = offset.top;
	    var bottomspace = ($(window).height() - (offset.top + program_width));
	    if (topspace >= bottomspace) {
	    	var ycoord = Math.max(offset.top - detail_panel_height, 0);
	    } else {
	    	var ycoord = Math.min(offset.top + program_height, $(window).height() - detail_panel_height);
	    }

	    $('.panel-body img').attr('src', thumbnail_url);

	    var titlehtml = event_title;

	    dpanel_title.html(titlehtml);
	    dpanel_playtime.html(play_range);
	    dpanel_desc.html(newdesc);
	    dpane.css({ 'left': xcoord, 'top': ycoord, 'display': 'block'});
		// $('#remind-button').focus();
	  };

	  var handler_out = function(e) {
		  $('#details-panel').css('display','none');
	  };
	  $('div.tv-program table.table tbody tr td').on('mouseover', handler_in).on('mouseout', handler_out).on('focusin', handler_in).on('focusout', handler_out);

	  // when hovering the details pane keep displayed, otherwise hide
	  $('#details-panel').on('mouseover', function(e) {
	      $(this).css('display','block');
	  });
	  $('#details-panel').on('mouseout', function(e) {
		  if (e == null) {
			  return;
		  }
	    // this is the original element the event handler was assigned to
	    var e = e.toElement || e.relatedTarget;

	    // if mouse out children of current element, ignore it & do nothing.
	    $.each($(this).children(), function(index, item) {
	    	if (item == e) {
	    		return;
	    	}
	    })
	    $(this).css('display','none');
	  });

	$('#remind-button').click(function(event) {
		event.preventDefault();
		var eventTitle = currentFocusedEvent.title;
	    var channelNum = currentFocusedEvent.channel;
	    var startTime = currentFocusedEvent.start;
	    var endTime = currentFocusedEvent.end;
		//console.log("remind clicked, channelNum:" + channelNum + " startTime: " + startTime + " endTime: " + endTime + " typeof time:" + typeof endTime);
		EPGUtils.remind(eventTitle, channelNum, startTime, endTime);
		$('#details-panel').css('display','none');
	});

	$('#record-button').click(function(event) {
		event.preventDefault();
		var eventTitle = currentFocusedEvent.title;
	    var channelNum = currentFocusedEvent.channel;
	    var startTime = currentFocusedEvent.start;
	    var endTime = currentFocusedEvent.end;
		//console.log("record clicked, channelNum:" + channelNum + " startTime: " + startTime + " endTime: " + endTime + " typeof time:" + typeof endTime);
		EPGUtils.record(eventTitle, channelNum, startTime, endTime);
		$('#details-panel').css('display','none');
	});
};

var searchByKeyword = function(keywords) {
	 var channels = $("li.ets-item");
	 var events = $("div.tv-program table.table tbody tr td");
	 if (keywords === "") {
		 events.show();
		 channels.show();
         currentChannelPage = 0;
	 } else {
		 var keywordsRegexp = '[A-Z]*';

		 for (var i=0; i < keywords.length; i++) {
			 keywordsRegexp += (keywords.charAt(i));
			 if (i == (keywords.length - 1)) {
				 keywordsRegexp += '[A-Z]*';
			 } else {
				 keywordsRegexp += '[A-Z]*\\s';
			 }
		 }

		 var channels_filter = function (index) {
			 var material = $(this).find('div.tv-name span.tn img').attr('keyword');
			 //console.log("channel material: " + material);
			 //console.log("channel keywordsRegexp: " + keywordsRegexp);
			 var re = new RegExp(keywordsRegexp, "ig");
			 return re.test(material);
		 }

		 var matchedChannels = channels.filter(channels_filter);
		 // if any channel matched, won't continue search event
		 if (matchedChannels.length > 0) {
			 channels.hide();
			 matchedChannels.show()
			 return;
		 }

		 var eventsFilter = function (index) {
			 var material = $( this ).attr( "keyword" );
			 //console.log("step 2 keywordsRegexp: " + keywordsRegexp);
			 var re = new RegExp(keywordsRegexp, "ig");
			 return re.test(material);
		 }
		 var matchedEvents = events.filter(eventsFilter);

		 if (matchedEvents.length > 0) {
			 events.hide();
			 matchedEvents.show()
		 }

		 if (matchedEvents.length == 0) {
			 channels.hide();
		 }
	 }
     MRVLEpg.Panel.display_page($('ul.ets-list li.ets-item:visible'), currentChannelPage);
     $("li div.tv-program:visible:first tr td:first").focus();
}

function onKeyUp(key) {
	//console.log('faywong onKeyUp() + key: ' + key);
	if (key == 'red') {
		$('#details-panel').is(':visible') && $('#remind-button').trigger('click');
	} else if (key == 'green') {
		$('#details-panel').is(':visible') && $('#record-button').trigger('click');
	} else if (key == 'yellow') {

	} else if (key == 'blue') {
	} else if (key == 'back') {
		$('#details-panel').css('display','none');
		searchByKeyword('');
	} else {
		if (!$("#search-input").is(":focus")) {
			$("#search-input").focus();
			$("#search-input").val($("#search-input").val() + key.toLowerCase());
			searchByKeyword($.trim($("#search-input").val()));
		}
	}
}

var initEpgHeader = function() {
	syncCurrentTime();
	var text = $("#search-input").val();
	var clean_input = function(e) {
		if ($(e.target).val() == text || $(e.target).val() == '') {
			$(e.target).val('');
		}
	};
	$("#search-input").keydown(clean_input);
	$("#search-input").mousedown(clean_input);

    $("#search-input").on("keyup", function () {
        searchByKeyword($.trim($(this).val()));
     });

	var handler_in = function(event) {
		$(this).children("div.drop-panel").stop(true,true).slideDown(600);
	};
	var handler_out = function(event) {
		$(this).children("div.drop-panel").stop(true,true).slideUp("fast");
	};

	$('#PREV_DAY_BTN, #NEXT_DAY_BTN, #LEFT_TIME_BTN, #RIGHT_TIME_BTN, .control-search, tr td').on('focusin', function(event) {
		////console.log('other elements focus');
		$('li#nav_category').focusin(handler_in);
		$('li#nav_time_interval').focusin(handler_in);
	});
	$("li#nav_category, li#nav_time_interval").focusin(function () {
		////console.log('nav_category or nav_time_interval element focus in');
		handler_in();
		$(this).unbind('focusin');
	});
	$("li#nav_category, li#nav_time_interval").focusout(handler_out);
	$("li#nav_category, li#nav_time_interval").hover(handler_in, handler_out);

/*	var INTERVAL_BTN = $.cookie("INTERVAL_BTN");
	$("li#nav_time_interval ul.dp-list li").filter('li[title=\''+INTERVAL_BTN+'\']').addClass('select')
				.siblings().removeClass('select');*/
	$("#search-input").focus();

};

$(document).ready(function() {

	MRVLEpg = (function() {

		var _constants = {
				"APP_URL"		:	"http://live.pps.tv/index.php/epg/",
				"ATTACH_PATH"	:	"http://live.pps.tv/attached/"
		}

		var _mothod_params = {
				"CHANNEL_ACT"	:	'get_channel_list',
				"PERIOD_ACT"	:	'get_time_period',
				"PROGRAM_ACT"	:	'get_program_list_by_timezone'
		}

		var _week_arr = {
				"1"		:	"一",
				"2"		:	"二",
				"3"		:	"三",
				"4"		:	"四",
				"5"		:	"五",
				"6"		:	"六",
				"0"		:	"日"
		}

		_constants['MOTHOD_PARAMS']	=	_mothod_params;
		_constants['WEEK_ARR']	=	_week_arr;

		// Privileged static method.
        var _const = function () {};
        _const.constant = function (name) { return _constants[name]; };
        return _const;
	})();

	/**
	 *
	 * 当前缓存
	 *
	 */

	MRVLEpg.Cache = {
		CATEGORY			:	'全部',
		OFFSET_LEN			:	4,		//偏移两个
		CELL_NUM_PER_PANEL  :	4,		//每版显示时间块个数
		CURR_DATE			:	'',		//版面日期
		PROLIST_TIME		:	'',		//时间框架
		INTERVAL_PER_CELL   :	'',		//时间间隔
		PREV_DAY_BTN		:	'',		//前一天
		NEXT_DAY_BTN		:	'',		//后一天
		CURRENT_DAY_BTN		:	'',		//返回当前
		LEFT_TIME_BTN		:	'',		//时间轴 左按钮
		RIGHT_TIME_BTN		:	'',		//时间抽 右按钮
		ANIMATE_AUTOLOAD	:	10,		//多少秒更新一次
		CHANNELS_PER_PAGE   :   5
	}

	function TimeItem (h, m) {
		this.hour = h;
		this.min = m;
	}

	TimeItem.prototype.inc = function(delta) {
		this.hour += Math.floor((this.min + delta) / 60);
		this.min = (this.min + delta) % 60;
	}

	TimeItem.prototype.toString = function() {
		return '%02d:%02d'.sprintf(this.hour, this.min);
	}

	function get_time_series(interval) {
		//console.log("get_time_series(" + interval + ") in");
		var time_series = [];
		var start = new TimeItem(0, 0);
		var end = new TimeItem(24, 0);
		while (JSON.stringify(start) != JSON.stringify(end)) {
			time_series.push(start.toString());
			////console.log("Pushed an time item: " + start.toString());
			start.inc(interval);
		}
		////console.log("The final time_series: " + time_series);
		return time_series;
	}

	initEpgHeader();

	/**
	 *
	 *
	 * 初始化频道列表、时间轴
	 *
	 *
	 */

	MRVLEpg.Panel = {
		parent 		 : MRVLEpg,
		channelPanel : null,
		timelinePanel: null,
		channelPanelInit : function(data, callback) {
			// retrieve channel list here
			(function(data){
				try {
					var html = '';
					$.each(data , function(channelInx, channel) {
						var visible = (currentChannelPage * MRVLEpg.Cache.CHANNELS_PER_PAGE <= channelInx
							           && channelInx < (currentChannelPage + 1)* MRVLEpg.Cache.CHANNELS_PER_PAGE);
						//console.log("index: " + channelInx + " channel:" + channel + " visible:" + visible);
						var $channel_text = '';
						if (channel.hasOwnProperty('link_url')) {
							$channel_text = '<a href="'+ channel['link_url']+'" target=""><b>'+channel['name']+'</b></a>';
						}else{
							$channel_text = '<b>'+channel['name']+'</b>'
						}

						html += '<li class="ets-item" id="channel_Id_' + channel['Id'] + '"';
						if (!visible) {
							//console.log("invisible case, channel index:" + channelInx);
							html += ' style="display: none;"';
						}
						html +='>'
						html += '<div class="tv-name"><span class="tn"><img src="'+ MRVLEpg.constant("ATTACH_PATH")+channel['logo_url']+'" width="23" height="23" alt=""' + 'serviceid="' + channel['service_id']+ '" keyword="' + channel['pinyin_keyword'] +  '"' + ' /> '+$channel_text+'</span></div>';
						html += '<div class="tv-program"><table class="table"><tr><td><div class="tp">暂无节目内容</div></td></tr></table></div>';
						html += '</li>';
					});
					$("div#epg_tvchannel_list ul").html(html);
					if (typeof callback == 'function') callback.apply(null);
				} catch (e) {
					alert(e);
				}
			})(data);
		},
		navi_to_page : function(ets_items, target, old) {
			//console.log('target page:' + target + ' old:' + old);
			if (target < 0 || old < 0) {
                currentChannelPage = 0;
                return;
            }

            if(ets_items == undefined || ets_items == null) {
				return;
			}
			var max_pages = (ets_items.length + MRVLEpg.Cache.CHANNELS_PER_PAGE - 1) / MRVLEpg.Cache.CHANNELS_PER_PAGE;
            //console.log('max_pages:' + max_pages);
			if (target >= Math.floor(max_pages)) {
                currentChannelPage = 0;
				return;
			}

			$.each(ets_items, function(etsInx, item) {
				var new_page = (target * MRVLEpg.Cache.CHANNELS_PER_PAGE <= etsInx
					           && etsInx < (target + 1) * MRVLEpg.Cache.CHANNELS_PER_PAGE);
				var old_page = (old * MRVLEpg.Cache.CHANNELS_PER_PAGE <= etsInx
					           && etsInx < (old + 1) * MRVLEpg.Cache.CHANNELS_PER_PAGE);
				//console.log("etsInx: " + etsInx + " item:" + item);
				if (new_page) {
					$(item).css( "display", "block");
					//console.log('channel index:' + etsInx + ' display block');
				} else if (old_page) {
					$(item).css( "display", "none");
					//console.log('channel index:' + etsInx + ' display none');
				}
			});
			MRVLEpg.Event.bindChannelPaginatingHandler();
            currentChannelPage = target;
		},
        // force to show page with index "page", hidden others
        display_page : function(ets_items, page) {
            //console.log('display page:' + page);
            if (page < 0) {
                currentChannelPage = 0;
            }

            if (ets_items == undefined
                || ets_items == null) {
                return;
            }
            var max_pages = (ets_items.length + MRVLEpg.Cache.CHANNELS_PER_PAGE - 1) / MRVLEpg.Cache.CHANNELS_PER_PAGE;
            if (page >= Math.floor(max_pages)) {
                currentChannelPage = 0;
                return;
            }

            $.each(ets_items, function(etsInx, item) {
                var target_page = (page * MRVLEpg.Cache.CHANNELS_PER_PAGE <= etsInx
                               && etsInx < (page + 1) * MRVLEpg.Cache.CHANNELS_PER_PAGE);
                ////console.log("etsInx: " + etsInx + " item:" + item);
                if (target_page) {
                    $(item).css( "display", "block");
                    ////console.log('channel index:' + etsInx + ' display block');
                } else {
                    $(item).css( "display", "none");
                    ////console.log('channel index:' + etsInx + ' display none');
                }
            });
            MRVLEpg.Event.bindChannelPaginatingHandler();
            currentChannelPage = page;
        },
		timelinePanelInit : function(callback) {
			//初始化头部顶部时间轴
			var html = '';
			//console.log("MRVLEpg.Cache.INTERVAL_PER_CELL: " + MRVLEpg.Cache.INTERVAL_PER_CELL);
			var time_series = get_time_series(parseInt(MRVLEpg.Cache.INTERVAL_PER_CELL));
			$.each(time_series , function(commentIndex, time) {
				html += '<li class="tq-item">';
				html += time;
				html += '</li>';

			});
			$("ul#prolist_time").html(html);
			$(".tq-item").css("width", MRVLEpg.Event.panel_width / MRVLEpg.Cache.CELL_NUM_PER_PANEL);
			//console.log("timelinePanelInit panel_width: " + MRVLEpg.Event.panel_width / MRVLEpg.Cache.CELL_NUM_PER_PANEL);
			if (typeof callback == 'function') callback.apply(null);
		}
	}

	function currentDateLocation() {
		var myDate = currentDate();
		var now_date = myDate.getFullYear()+'-'+(myDate.getMonth()+1)+'-'+myDate.getDate();
		if (MRVLEpg.Cache.CURR_DATE < now_date) {
			return -1;
		} else if (MRVLEpg.Cache.CURR_DATE > now_date) {
			return 1;
		} else if (MRVLEpg.Cache.CURR_DATE == now_date) {
			return 0;
		}
	}

	/**
	 *
	 * 事件响应
	 *
	 */

	MRVLEpg.Event = {
		parent 		   : 	MRVLEpg,
		parent_element : 	'',
		page		   :	1,		//当前页面下标
		li_size		   :    '',
		page_count	   :	'',		//只要不是整数，就往大的方向取最小的整数
		panel_width	   :	'',		//面板宽度	不带单位
		none_unit_width:	'',		//每次偏移宽度  不带单位
		is_refresh     :	false,	//是否强制刷新
		page_offset   :     0,     //页面与当前面板之间的偏差
		unusual		   :	false,
		start_time     :    '',
		persistentStartCellIndex : 0,
		startCellIndex :    0,
		wholeCellsNum  :    0,
		initialize	   :    function(load_data) {
			//console.log("MRVLEpg.Event initialized");
			// 根据当前时间 定位时间轴时间
			var $basic_num = 60 / MRVLEpg.Cache.INTERVAL_PER_CELL;
			var myDate = currentDate();
			var hours 	= myDate.getHours();
			var minutes = myDate.getMinutes();
			this.wholeCellsNum = 24 * 60 / MRVLEpg.Cache.INTERVAL_PER_CELL;
			var prolistWidth = this.wholeCellsNum * MRVLEpg.Event.panel_width / MRVLEpg.Cache.CELL_NUM_PER_PANEL;
			$("ul#prolist_time").css("width", prolistWidth);
			$(".tq-item").css("width", MRVLEpg.Event.panel_width / MRVLEpg.Cache.CELL_NUM_PER_PANEL);

			//console.log("hours: " + hours + " minutes: " + minutes);
			this.persistentStartCellIndex = this.startCellIndex = parseInt(parseInt(hours)*$basic_num)+parseInt(Math.floor(minutes/MRVLEpg.Cache.INTERVAL_PER_CELL));
			//console.log("startCellIndex: " + this.startCellIndex + " this.wholeCellsNum: " + this.wholeCellsNum);
			if (parseInt(this.startCellIndex) <= 0) {
				this.startCellIndex = 0;
			}
			var offsetPos = 0;
			if (this.startCellIndex + MRVLEpg.Cache.CELL_NUM_PER_PANEL > this.wholeCellsNum) {
				//console.log("this.page_count: " + this.page_count + " this.panel_width: " + this.panel_width + " this.li_size: " + this.li_size);
				this.page = this.page_count;
				this.page_offset = 0;
				offsetPos = 0 - this.panel_width/MRVLEpg.Cache.CELL_NUM_PER_PANEL*(this.li_size - MRVLEpg.Cache.CELL_NUM_PER_PANEL);
			} else {
				this.page = Math.ceil(this.startCellIndex/MRVLEpg.Cache.OFFSET_LEN);
				this.page_offset = this.page * MRVLEpg.Cache.OFFSET_LEN - this.startCellIndex;
				//console.log("step 2 this.page: " + this.page + " this.none_unit_width: " + this.none_unit_width);
				offsetPos = 0 - this.none_unit_width / MRVLEpg.Cache.OFFSET_LEN * this.startCellIndex;
				//console.log("step 3 offsetPos: " + offsetPos);
			}

			this.parent_element.css({ left : offsetPos });
			if (this.parent_element.find("li:eq("+ this.startCellIndex + ")").text()) {
				var timeString = this.parent_element.find("li:eq("+ this.startCellIndex +")").text();
				var dateArray = timeString.split(":");
				var cells = parseInt(dateArray[0], 10) + Math.ceil(parseInt(dateArray[1], 10) / 60);
				var tvProgramWidth = cells * MRVLEpg.Event.panel_width / MRVLEpg.Cache.CELL_NUM_PER_PANEL;
				// set the tv-program width
				$("div.tv-program").css("width", MRVLEpg.Event.panel_width).css("overflow", "hidden");
				MRVLEpg.Cache.PROLIST_TIME.attr('content', timeString);
			}

			//console.log("MRVLEpg.Cache.PROLIST_TIME content: " + MRVLEpg.Cache.PROLIST_TIME.attr('content') + " this.startCellIndex: " + this.startCellIndex);

			if (load_data) {
				this.populateEpgEvents();	//加载数据
			}

			this.animateShadow(true);
		},
		rightOffset : function() {
			if (!this.parent_element.is(":animated")) {
				//console.log("case 1 this.startCellIndex:" + this.startCellIndex + " this.wholeCellsNum:" + this.wholeCellsNum);
				var freeCellsNum = this.wholeCellsNum - this.startCellIndex;
				if (MRVLEpg.Cache.OFFSET_LEN < freeCellsNum && freeCellsNum < 2 * MRVLEpg.Cache.OFFSET_LEN) {
					this.startCellIndex = this.wholeCellsNum - MRVLEpg.Cache.OFFSET_LEN;
					//console.log("case 2 this.startCellIndex:" + this.startCellIndex);
				} else {
					this.startCellIndex = (this.startCellIndex + MRVLEpg.Cache.OFFSET_LEN) % this.wholeCellsNum;
					//console.log("case 3 this.startCellIndex:" + this.startCellIndex);
				}
				var pageFlip = false;
				if (this.startCellIndex == 0) {
					pageFlip = true;
					MRVLEpg.Cache.NEXT_DAY_BTN.triggerHandler("click");
				}
				var oldLeftOffset = parseInt(this.parent_element.css("left"));
				var leftOffset = 0 - this.startCellIndex * this.panel_width / MRVLEpg.Cache.CELL_NUM_PER_PANEL;
				var placement = leftOffset - oldLeftOffset;
				if (pageFlip) {
					placement = - (this.panel_width / MRVLEpg.Cache.CELL_NUM_PER_PANEL * MRVLEpg.Cache.OFFSET_LEN);
				}
				this.parent_element.animate({ left : leftOffset }, 800);  //通过改变left值，达到每次换一个版面
				//$("div.tv-program").animate({ left : '+=' + placement }, 800);
				$("table.table").animate({ left : '+=' + placement }, 800, function() {
					MRVLEpg.Event.bindProgrammePaginatingHandler();
				});
				MRVLEpg.Cache.PROLIST_TIME.attr('content', this.parent_element.find("li:eq("+this.startCellIndex+")").text());
				this.animateShadow(true);
				//this.populateEpgEvents();
			}
		},
		leftOffset : function() {
			var $parent = MRVLEpg.Cache.PROLIST_TIME;
			if ( !$parent.is(":animated") ) {
				//console.log("left case 1 this.startCellIndex:" + this.startCellIndex + " this.wholeCellsNum:" + this.wholeCellsNum);
				var pageFlip = false;
				if (this.startCellIndex == 0) {
					pageFlip = true;
					MRVLEpg.Cache.PREV_DAY_BTN.triggerHandler("click");
				}
				var newStartIndex = this.startCellIndex - MRVLEpg.Cache.OFFSET_LEN;
				if (-MRVLEpg.Cache.OFFSET_LEN < newStartIndex && newStartIndex < 0) {
					this.startCellIndex = 0;
					//console.log("left case 2 this.startCellIndex:" + this.startCellIndex);
				} else {
					this.startCellIndex = (newStartIndex + this.wholeCellsNum) % this.wholeCellsNum;
					//console.log("left case 3 this.startCellIndex:" + this.startCellIndex);
				}
				var oldLeftOffset = parseInt(this.parent_element.css("left"));
				var leftOffset = 0 - this.startCellIndex * this.panel_width / MRVLEpg.Cache.CELL_NUM_PER_PANEL;
				var placement = leftOffset - oldLeftOffset;
				if (pageFlip) {
					placement = this.panel_width / MRVLEpg.Cache.CELL_NUM_PER_PANEL * MRVLEpg.Cache.OFFSET_LEN;
				}
				var newTime = this.parent_element.find("li:eq("+this.startCellIndex+")").text();
/*
				var timeArray = newTime.split(":");
				var targetHour = parseInt(timeArray[0], 10);
				var targetMin = parseInt(timeArray[1], 10);

				timeArray = MRVLEpg.Cache.PROLIST_TIME.attr('content').split(":");
				var startHour = parseInt(timeArray[0], 10);
				var startMin = parseInt(timeArray[1], 10);
				console.log("targetHouer:" + targetHour + " targetMin:" + targetMin + " startHour:" + startHour + " startMin:" + startMin);
				var outDate = (targetHour < startHour || (targetHour == startHour && targetMin <= startMin));
				console.log("outDate:" + outDate);*/
				var oldLeft = parseInt($("table.table").css("left"), 10);
				oldLeft += placement;
				if (oldLeft >= 0) {
					$("table.table").animate({ left : '0' }, 800, function() {
						MRVLEpg.Event.bindProgrammePaginatingHandler();
					});
					this.startCellIndex = this.persistentStartCellIndex;
					newTime = this.parent_element.find("li:eq("+this.startCellIndex+")").text();
					leftOffset = 0 - this.startCellIndex * this.panel_width / MRVLEpg.Cache.CELL_NUM_PER_PANEL;
					this.parent_element.animate({ left : leftOffset }, 800);  //通过改变left值，达到每次换一个版面
				} else {
					$("table.table").animate({ left : '+=' + placement }, 800, function() {
						MRVLEpg.Event.bindProgrammePaginatingHandler();
					});
					this.parent_element.animate({ left : leftOffset }, 800);  //通过改变left值，达到每次换一个版面
				}
				MRVLEpg.Cache.PROLIST_TIME.attr('content', newTime);
				this.animateShadow(true);
				//this.populateEpgEvents();
				this.bindProgrammePaginatingHandler();
			}
		},
		animateShadow : function(ref_timeline) {
			switch (currentDateLocation()) {
			case -1:
				$("div.time-schedule div.ts-width").css({"width" : "100%"});
				if (ref_timeline) {
					$("#Pos_timeline").hide();
				}
				return false;
				break;
			case 1:
				$("div.time-schedule div.ts-width").css({"width" : "0%"});
				if (ref_timeline) {
					$("#Pos_timeline").hide();
				}
				return false;
				break;
			case 0:
				{
					var $total_width = $("div.time-schedule").width();
					//console.log("$total_width: " + $total_width);
					var $duration = parseInt(MRVLEpg.Cache.INTERVAL_PER_CELL)*parseInt(MRVLEpg.Cache.CELL_NUM_PER_PANEL);
					var $minute_len = $total_width/$duration;
					var $startTime = MRVLEpg.Cache.PROLIST_TIME.attr('content');
					//console.log("$startTime: " + $startTime);
					var myDate = currentDate();
					//console.log("myDate: " + myDate.getHours() + ":" + myDate.getMinutes());
					var date_arr = $startTime.split(":");
					var param_time1 = parseInt(date_arr[0]*60, 10) + parseInt(date_arr[1], 10);
					var param_time2 = parseInt(myDate.getHours()*60, 10) + parseInt(myDate.getMinutes(), 10);
					//console.log("param_time1: " + param_time1 + " param_time2: " + param_time2);
					var offset_pos = ((param_time2-param_time1)*$minute_len)/$total_width*100;
					//console.log("offset_pos: " + offset_pos);
					if(!isNaN(offset_pos)){
						if(offset_pos < 0){
							offset_pos = 0;
						}
						$("div.time-schedule div.ts-width").css({"width" : offset_pos+"%"});

						if (ref_timeline) {
							if(offset_pos == 0){
								$("#Pos_timeline").hide();
							}else{
								$("#Pos_timeline").css({"left" : offset_pos+"%"}).show();
							}
						}
					}
				}
				break;
			}
		},
		populateEpgEvents : function() {
			var timeArray = MRVLEpg.Cache.PROLIST_TIME.attr('content').split(":");
			var dateArray = MRVLEpg.Cache.CURR_DATE.split("-");
			var startTimeInUTCSeconds = Date.UTC(Number(dateArray[0]), Number(dateArray[1])-1, Number(dateArray[2]), timeArray[0], timeArray[1]) / 1000;
			//console.log("faywong startTimeUTC: " + startTimeUTC);
			var durationAcrossWholePanel = parseInt(MRVLEpg.Cache.INTERVAL_PER_CELL) * parseInt(MRVLEpg.Cache.CELL_NUM_PER_PANEL);
			var jsonString = EPGUtils.getEPG(startTimeInUTCSeconds, durationAcrossWholePanel * 60);
			////console.log("jsonString:" + jsonString);
			epgEvents = JSON.parse(jsonString);
			(function(data) {
				if (data == undefined || data == null) return;
				var $total_width = MRVLEpg.Event.panel_width;
				var $minute_len = $total_width / durationAcrossWholePanel;
				var $tvchannel_list_ul = $("div#epg_tvchannel_list ul");

				for (var i in data) {
					var html = '';

					if ($tvchannel_list_ul.find("li[id='channel_Id_"+i+"']")[0]) {
						$.each( data[i] , function(epgEventInx, epgEvent) {
							var eventWidth = epgEvent['duration'] / 60 *$minute_len;

							/**
							 * 过滤截止字符
							 */
							var epg_title = epgEvent['title'];
							if (4 <= eventWidth && eventWidth<6) {
								epg_title = '...';
							} else if(eventWidth < 4){
								epg_title = '';
							}
							var class_name = epgEvent['playing'] ? 'playing' : '';

							var title = epgEvent['title'];
							var keyword = epgEvent['pinyin_keyword'];
							//var relativeWidth = eventWidth * 100 / $(".tv-program").width();
							////console.log("duration:" + epgEvent['duration'] + " relativeWidth:" + relativeWidth + " eventWidth:" + eventWidth + " tv-program width:" + $(".tv-program").width());
							html += '<td tabindex="0" width="' + eventWidth + 'px" class="' + class_name + '" title="' + title + '" thumbnail_url="' + epgEvent['thumbnail_url'] + '" play_range="' + epgEvent['play_range'] + '" keyword="'+ keyword + '" channel_num="' + epgEvent['channel_num'] + '" start_time="'+ epgEvent['start_time'] + '" end_time="'+ epgEvent['end_time'] + '" detail_desc="'+ epgEvent['detail_desc'] + '" detail_desc="'+ epgEvent['short_desc'] + '" content_category="'+ epgEvent['content_category'] + '">';
							html += '<div class="tp"><span class="ico-play ico"></span>';
							html += '<a href="#" target="_blank">' + epg_title +'</a>';
							html += '</div>';
							html += '</td>';
						});
						var $parent_li = $tvchannel_list_ul.find("li[id='channel_Id_" + i +"']");
						$parent_li.find("table tr").html(html);
						$parent_li.find("table tr td:last").attr('class', 'last');
					}
				}
				//console.log("NO. of td:" + $("td").length);
			})(epgEvents);
			// //console.log('faywong step 111');
			// //console.log('playing tds:' + $('.tv-program table.table tbody tr td.playing .tp a'));
			$('.tv-program table.table tbody tr td.playing .tp a').click(function (event) {
				// //console.log("faywong playing td clicked!");
				event.preventDefault();
				var channel_num = $(this).parent().parent().attr('channel_num');;
				//console.log("playing clicked, channel_num:" + channel_num);
				EPGUtils.playback(channel_num);
			});
			MRVLEpg.Event.bindChannelPaginatingHandler();
			MRVLEpg.Event.bindProgrammePaginatingHandler();
		},
		bindChannelPaginatingHandler : function() {
			// after populating events, bind event handlers for last visible elements to handle
			// user navi down/up event
			$("li div.tv-program:visible:last").find('table.table tbody tr td').keydown(function (event) {
				if (event.keyCode == 40) {
				      MRVLEpg.Panel.navi_to_page($('ul.ets-list li.ets-item'), currentChannelPage + 1, currentChannelPage++);
                      $("li div.tv-program:visible:first tr td:first").focus();
				}
				console.log('handler for last visible td keydown called, keycode:' + event.keyCode);
			});
			$("li div.tv-program:visible:first").find('table.table tbody tr td').keydown(function (event) {
				if (event.keyCode == 38) {
				    MRVLEpg.Panel.navi_to_page($('ul.ets-list li.ets-item'), currentChannelPage - 1, currentChannelPage--);
                    $("li div.tv-program:visible:first tr td:first").focus();
				}
				console.log('handler for first visible td keydown called, keycode:' + event.keyCode);
			});
		},
		// TODO:
		bindProgrammePaginatingHandler : function() {
			// after populating events, bind event handlers for first & last visible programme event to handle
			// user navi left/right event
			$(".ets-item").each(function () {
				$(this).find(".table td").unbind("keydown");
			    var tvProgramme = $(this).children(".tv-program");
			    var offset = tvProgramme.offset();
				//console.log("tvProgramme offset left:" + offset.left + " top:" + offset.top);
				offset.rightDirection = true;
				var firstCell = $(this).find(".table td").closestToOffset(offset);
				if (!firstCell || firstCell.length == 0) {
					return;
				}
				//console.log("firstCell title:" + firstCell.attr('title'));
				offset.left += tvProgramme.outerWidth();
				offset.rightDirection = false;
				var lastCell = $(this).find(".table td").closestToOffset(offset);
				if (!lastCell || lastCell.length == 0) {
					return;
				}
				//console.log("lastCell title:" + lastCell.attr('title'));

				firstCell.bind("keydown", function (event) {
					// left key pressed
					console.log("firstCell keydown, cell title:" + $(this).attr('title'));
					if (event.keyCode == 37) {
						MRVLEpg.Event.leftOffset();
					}
				});

				lastCell.bind("keydown", function (event) {
					// right key pressed, if NULL Event, don't right flip
					console.log("lastCell keydown, cell title:" + $(this).attr('title'));
					if (event.keyCode == 39 && ($(this).attr('class') != 'last')) {
						MRVLEpg.Event.rightOffset();
					}
				});
			});
		}
	}

	var syncTimelinePanel = function() {
		//console.log("the new interval is: " + MRVLEpg.Cache.INTERVAL_PER_CELL);

		MRVLEpg.Panel.timelinePanelInit(function() {
			MRVLEpg.Event.li_size		= ($('ul#prolist_time li').length >=1 ? $('ul#prolist_time li').length : null);
			MRVLEpg.Event.panel_width	= ($('div.tq-width').length == 1 ? $('div.tq-width').width()+2 : null);
			MRVLEpg.Event.none_unit_width =	MRVLEpg.Event.panel_width/(MRVLEpg.Cache.CELL_NUM_PER_PANEL/MRVLEpg.Cache.OFFSET_LEN);
			MRVLEpg.Event.page_count = Math.ceil((MRVLEpg.Event.li_size-MRVLEpg.Cache.CELL_NUM_PER_PANEL + MRVLEpg.Cache.OFFSET_LEN)/MRVLEpg.Cache.OFFSET_LEN);
			MRVLEpg.Event.initialize(true);
		});
	}

	/**
	 *
	 * bussniss	function
	 * 业务方法
	 *
	 */
	var myDate = currentDate();
	MRVLEpg.Cache.CURR_DATE = myDate.getFullYear()+'-'+(myDate.getMonth()+1)+'-'+myDate.getDate();

	var date_arr = MRVLEpg.Cache.CURR_DATE.split("-");
	var newdt = new Date(Number(date_arr[0]),Number(date_arr[1])-1,Number(date_arr[2]));
    var weekarr = MRVLEpg.constant('WEEK_ARR');

    $currer_showtime = (newdt.getMonth()+1)+'月'+newdt.getDate()+'日  星期'+weekarr[newdt.getDay()];
    $("div.epg-date").text($currer_showtime);

	MRVLEpg.Cache.PROLIST_TIME = ($('#prolist_time').length == 1 ? $('#prolist_time') : null);
	MRVLEpg.Event.parent_element = MRVLEpg.Cache.PROLIST_TIME;

	var timeLineInterval = $("li#nav_time_interval ul.dp-list li").filter('.select').attr("title");

	MRVLEpg.Cache.INTERVAL_PER_CELL = timeLineInterval;
	syncTimelinePanel();

	MRVLEpg.Cache.PREV_DAY_BTN = ($('#PREV_DAY_BTN').length == 1 ? $('#PREV_DAY_BTN') : null);
	MRVLEpg.Cache.NEXT_DAY_BTN = ($('#NEXT_DAY_BTN').length == 1 ? $('#NEXT_DAY_BTN') : null);
	MRVLEpg.Cache.CURRENT_DAY_BTN = ($('#CURRENT_DAY_BTN').length == 1 ? $('#CURRENT_DAY_BTN') : null);
	MRVLEpg.Cache.LEFT_TIME_BTN = ($('#LEFT_TIME_BTN').length == 1 ? $('#LEFT_TIME_BTN') : null);
	MRVLEpg.Cache.RIGHT_TIME_BTN = ($('#RIGHT_TIME_BTN').length == 1 ? $('#RIGHT_TIME_BTN') : null);

	if (MRVLEpg.Cache.PROLIST_TIME == null || MRVLEpg.Cache.PREV_DAY_BTN == null || MRVLEpg.Cache.NEXT_DAY_BTN == null || MRVLEpg.Cache.LEFT_TIME_BTN == null ||
			MRVLEpg.Cache.RIGHT_TIME_BTN == null || MRVLEpg.Cache.CURRENT_DAY_BTN == null){
			alert('页面初始化失败！');
			return;
	}
	// 初始化页面

	MRVLEpg.Event.li_size		=	($('ul#prolist_time li').length >=1 ? $('ul#prolist_time li').length : null);
	MRVLEpg.Event.panel_width	=	($('div.tq-width').length == 1 ? $('div.tq-width').width()+2 : null);
	//console.log("MRVLEpg.Event.panel_width: " + MRVLEpg.Event.panel_width);
	MRVLEpg.Event.none_unit_width=	MRVLEpg.Event.panel_width/(MRVLEpg.Cache.CELL_NUM_PER_PANEL/MRVLEpg.Cache.OFFSET_LEN);
	MRVLEpg.Event.page_count 		= 	Math.ceil((MRVLEpg.Event.li_size-MRVLEpg.Cache.CELL_NUM_PER_PANEL+MRVLEpg.Cache.OFFSET_LEN)/MRVLEpg.Cache.OFFSET_LEN);

	MRVLEpg.Event.initialize();
	/// 监听事件

	MRVLEpg.Cache.PREV_DAY_BTN.click(function() {
	    var date_arr = MRVLEpg.Cache.CURR_DATE.split("-");
	    var newdt = new Date(Number(date_arr[0]),Number(date_arr[1])-1,Number(date_arr[2])-1);
	    MRVLEpg.Cache.CURR_DATE = newdt.getFullYear() + "-" +   (newdt.getMonth()+1) + "-" + newdt.getDate();
	    var weekarr = MRVLEpg.constant('WEEK_ARR');

	    $currer_showtime = (newdt.getMonth()+1)+'月'+newdt.getDate()+'日  星期'+weekarr[newdt.getDay()];
	    $("div.epg-date").text($currer_showtime);
	    // TODO:
	    //MRVLEpg.Event.populateEpgEvents();
	    MRVLEpg.Event.animateShadow(true);
	});

	MRVLEpg.Cache.NEXT_DAY_BTN.click(function() {
		var date_arr = MRVLEpg.Cache.CURR_DATE.split("-");
	    var newdt = new Date(Number(date_arr[0]),Number(date_arr[1])-1,Number(date_arr[2])+1);
	    MRVLEpg.Cache.CURR_DATE = newdt.getFullYear() + "-" +   (newdt.getMonth()+1) + "-" + newdt.getDate();
	    var weekarr = MRVLEpg.constant('WEEK_ARR');

	    $currer_showtime = (newdt.getMonth()+1)+'月'+newdt.getDate()+'日  星期'+weekarr[newdt.getDay()];
	    $("div.epg-date").text($currer_showtime);
	    // TODO:
	    //MRVLEpg.Event.populateEpgEvents();
	    MRVLEpg.Event.animateShadow(true);
	});

	MRVLEpg.Cache.CURRENT_DAY_BTN.click(function() {
		var myDate = currentDate();
	    $("div.epg-date").text((myDate.getMonth()+1)+'月'+myDate.getDate()+'日  星期'+weekarr[myDate.getDay()]);
		MRVLEpg.Cache.CURR_DATE = myDate.getFullYear()+'-'+(myDate.getMonth()+1)+'-'+myDate.getDate();
		MRVLEpg.Event.is_refresh = true;
		MRVLEpg.Event.initialize(true);
	});

	MRVLEpg.Cache.LEFT_TIME_BTN.click(function(){
		MRVLEpg.Event.leftOffset();
		return false;
	});

	MRVLEpg.Cache.RIGHT_TIME_BTN.click(function(){
		MRVLEpg.Event.rightOffset();
		return false;
	});

	$("li#nav_time_interval ul.dp-list li").click(function() {
		//console.log('nav_time_interval click() in');
		$(this).addClass('select')
		   .siblings().removeClass('select');

		MRVLEpg.Cache.INTERVAL_PER_CELL = $(this).attr('title');
		syncTimelinePanel();
		$(this).parents('.drop-panel').slideUp();
	});

	var syncContentCategoryList = function (content_category_list) {
		if (content_category_list == null || content_category_list == undefined) {
			return;
		}
		$("li#nav_category ul.dp-list").empty();
		$.each(content_category_list , function(index, content_type) {
			var child_element = '<li content_category="' + content_type + '" class="dp-item"><a class="ui-link" href="#">' + content_type + '</a></li>';
			$("li#nav_category ul.dp-list").append(child_element);
		});
	};

	syncContentCategoryList(JSON.parse(EPGUtils.getContentTypeList()));

	MRVLEpg.Cache.CATEGORY = $.cookie('content_category');

	var lastCategory = $("li#nav_category ul.dp-list li").filter('[content_category=\"'+ MRVLEpg.Cache.CATEGORY +'\"]');

	//console.log('lastCategory length: ' + lastCategory.length);

	if (lastCategory.length == 0) {
		//console.log('category: ' + MRVLEpg.Cache.CATEGORY + ' doesn\'t exist');
		MRVLEpg.Cache.CATEGORY = '全部';
		$.cookie('content_category', MRVLEpg.Cache.CATEGORY);
	}

	//console.log('category: ' + MRVLEpg.Cache.CATEGORY);

	$("li#nav_category ul.dp-list li").filter('[content_category=\''+ MRVLEpg.Cache.CATEGORY +'\']').addClass('select')
		.siblings().removeClass('select');

	$("li#nav_category ul.dp-list li").click(function() {
		MRVLEpg.Cache.CATEGORY = $(this).attr('content_category');
		$.cookie('content_category', MRVLEpg.Cache.CATEGORY);
		$(this).addClass('select')
		   .siblings().removeClass('select');

		 var events = $("div.tv-program table.table tbody tr td");

		 if (MRVLEpg.Cache.CATEGORY === "ALL" || MRVLEpg.Cache.CATEGORY === "全部") {
			 events.show();
		 } else {
			 events.hide().filter('[content_category=\"' + MRVLEpg.Cache.CATEGORY + '\"]').show();
		 }
		$(this).parents('.drop-panel').slideUp();
	});

	window.setInterval(syncCurrentTime, MRVLEpg.Cache.ANIMATE_AUTOLOAD*1000);
	channelList = JSON.parse(EPGUtils.getChannelList());
	MRVLEpg.Panel.channelPanelInit(channelList, function(){
		MRVLEpg.Event.initialize(true);
	});
	initEpgDescPanel();
});
