	var verifyCode = 0;
	$(function() {
		$('.confirm-dialog').bind('click', function() {
			confirm();
		});

		$("#code").bind('keydown', function(event) {
			event = document.all ? window.event : event;
			if ((event.keyCode || event.which) == 13) {
				if (verifyCode == 1) {
					verify();
				} else {
					return false;
				}
			}
		});
		$("#code").bind(
				'keyup',
				function() {
					if ($(this).val().length == 6) {
						$.ajax({
							type : "post",
							url : "/checkAuthCode",
							data : {
								'code' : $("#code").val()
							},
							dataType : "json",
							success : function(re) {
								if (re.error == "true") {
									$(".modal-body p").html("验证成功！点击确认进行刷新")
											.addClass("green").removeClass(
													"red");
									verifyCode = 1;
								} else if (re.error == "false") {
									$("#code").focus();
									$(".modal-body p").html("输入验证码错误！")
											.addClass("red").removeClass(
													"green");
									verifyCode = 0;
								} else {
									$("#code").focus();
									$(".modal-body p").html("验证码超时！重新获取")
											.addClass("red").removeClass(
													"green");
									$('#captcha').attr(
											"src",
											"/api/captcha?switch=1?t="
													+ new Date().getTime());
									verifyCode = 0;
								}
							},
							error : function() {
								$("#code").focus();
								$(".modal-body p").html("验证码超时！请重新获取")
										.addClass("red");
								$('#captcha').attr(
										"src",
										"/api/captcha?switch=1?t="
												+ new Date().getTime());
								verifyCode = 0;
							}
						});
					} else {
						$(".modal-body p").html("请输入图片中的验证码").removeClass(
								"green").removeClass("red");
						verifyCode = 0;
					}
				});
	});

	function doAjax(json){
		buildIcpHtml(json);
		if (json['icp'] == '未备案') {
			$('icp-table-no').show();
			$('icp-table').hide();
		}else{
			$('icp-table-no').hide();
			$('icp-table').show();
			buildIcpHtml(json);
		}
		
		
	}
	function confirm() {
		$.ajax({
			url : "/queryNoCache?domain=" + $("#domain").val(),
			type : "get",
			dataType : "json",
			success : function(json) {
				console.log(json);
				if (json['state'] == 'no_resource') {
					console.log('没有资源了');
					_confirm();
					return false;
				} else {
					console.log('成功了....');
					$('.search-cache').html('<span class="green">更新成功</span>')
							.delay(2000).fadeOut();
					doAjax(json);
				}

			},
			error : function() {
				alert('失败!');
				return false;
			}
		});
	}
	function _confirm() {
		$('#captcha').attr("src", "/genAuthCode?t=" + new Date().getTime());
		$('#confirm-modal').show();
		$("#code").focus();
		$('.modal .close,.modal .no').bind(
				'click',
				function() {
					$('.modal').hide();
					$('.modal .captcha').attr("src",
							"/genAuthCode?t=" + new Date().getTime());
					$("#code").val('');
					$(".modal-body p").html('请输入图片中的验证码').removeClass();
					verifyCode = 0;
				});
		$('#captcha').bind('click', function() {
			$(this).attr("src", "/genAuthCode?t=" + new Date().getTime());
		});
	}


	function buildIcpHtml(object) {
		$('.cha-default').remove();
		var html = '<div class="title"><i class="ico-bar-vertical"></i><h4>备案信息</h4></div><div class="table" id="company"><table class="table">';
		if (!!object['company']) {
			html += '<tr><td class="thead">主办单位名称</td><td>' + object['company']
					+ '</td></tr>';
		}
		if (!!object['type']) {
			html += '<tr><td class="thead">主办单位性质</td><td>' + object['type']
					+ '</td></tr>';
		}
		if (!!object['icp']) {
			html += '<tr><td class="thead">网站备案/许可证号</td><td><span>'
					+ object['icp'] + '</span></td></tr>';
		}
		if (!!object['name']) {
			html += '<tr><td class="thead">网站名称</td><td>' + object['name']
					+ '</td></tr>';
		}
		if (!!object['homes']) {
			html += '<tr><td class="thead">网站首页网址</td><td>'
					+ object['homes'].replace(/\|/g, '<br />') + '</td></tr>';
		}
		if (!!object['domains']) {
			html += '<tr><td class="thead">网站域名</td><td>'
					+ object['domains'].replace(/\|/g, '<br />') + '</td></tr>';
		}
		if (!!object['owner'] && object['owner'] != 0) {
			html += '<tr><td class="thead">网站负责人</td><td>' + object['owner']
					+ '</td></tr>';
		}
		if (!!object['icp_time']) {
			var time_tmp = new Date(parseInt(object['icp_time']) * 1000), month = (time_tmp
					.getMonth() + 1) > 10 ? (time_tmp.getMonth() + 1) : '0'
					+ (time_tmp.getMonth() + 1), day = (time_tmp.getDate()) > 10 ? (time_tmp
					.getDate())
					: '0' + (time_tmp.getDate());

			html += '<tr><td class="thead">审核时间</td><td><span>'
					+ time_tmp.getFullYear() + '-' + month + '-' + day
					+ '</span></td></tr>';
		}
		html += '</table></div>';

		$('#icp-html').html(html);
	}

	function verify() {
		if ($("#code").val().length == 6 && verifyCode == 1) {
			$('.modal').hide();
			$(".search-cache").html(
					"正在更新中，请稍等 <img src=\"/images/loading.gif\"/>");
			$('.cha-default').html('正在更新...');
			$
					.ajax({
						url : "/queryWhithAuthCode",
						type : "post",
						data : {
							'domain' : $("#domain").val(),
							'code' : $("#code").val()
						},
						dataType : "json",
						success : function(json) {
							console.log(json);
							if (!!json['error']) {
								if (json['error'] == 'timeout') {
									str = '验证码超时';
								} else if (json['error'] == 'false') {
									str = '验证码错误';
								} else {
									str = '更新失败';
								}
								$(".search-cache")
										.html(
												str
														+ '，请重试 <a href="javascript:confirm();" class="confirm-dialog" title="刷新"><i class="ico-refresh"></i></a>');
								$('#confirm-modal').show();
								
								$('.update-icp')
										.html(
												'<div class="cha-default"><span class="red">'
														+ str
														+ '</span><a href="javascript:confirm();" class="confirm-dialog" title="刷新"><font color="gray" size="3"> >> 马上更新数据</font></a></div>');
								return false;
							}
							$('.search-cache').html(
									'<span class="green">更新成功</span>').delay(
									2000).fadeOut();
							doAjax(json);
						},
						error : function() {
							$(".search-cache")
									.html(
											'更新失败，请重试 <a href="javascript:confirm();" class="confirm-dialog" title="刷新"><i class="ico-refresh"></i></a>');
							return false;
						}
					});
		}
		$("#code").val('');
		$(".modal-body p").html('请输入图片中的验证码').removeClass();
		return false;
	}
	function url2domain(t) {
		return t.toLowerCase().replace("http://", "").replace("https://", "")
				.split("/")[0].split("?")[0].replace(/\s/g, "")
	}
	function isDomain(t) {
		var e = t.split(".");
		return !e[e.length - 1].match(/[0-9]/)
				&& !!t.match(/^[A-Za-z0-9_-]+(\.[A-Za-z0-9_-]+)+$/)
	}

	function referURL(t) {
		if (!!document.all) {
			var e = document.createElement("a");
			e.href = t, document.body.appendChild(e), e.click()
		} else
			location.href = t
	}
	function search_form() {
		var obj = document.getElementById('domain'), domain = url2domain(obj.value);
		if (domain) {
			if (isDomain(domain)) {
				referURL('/queryWitchCache?domain=' + domain);
			} else {
				alert('域名格式错误！');
				obj.focus();
			}
		} else {
			alert('请输入要查询的域名！');
			obj.focus();
		}
		return false;
	}
