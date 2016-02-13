var currentAddress = window.location.href;
$.getJSON(currentAddress+'.json',function(json){
    if(typeof json.layout !== 'undefined'){
        $.get(json.layout,function(layoutHtml){
            $(document.body).append(layoutHtml);
            deployContent(json);
        });
    }
});

function deployContent(json){
    if(typeof json.pageTitle !== 'undefined'){
        document.title = json.pageTitle;
    }else{
        document.title = 'no page title';
    }
	
	if(typeof json.studentName !== 'undefined' || json.studentName != ''){
        $('#footer').append('student name: '+json.studentName+'     ');
    }else{
		
    }
    
    if(typeof json.colorSetCSS !== 'undefined'){
        $('head').append('<link rel="stylesheet" href="'+json.colorSetCSS+'">');
    }else{
        $('head').append('<link rel="stylesheet" href="css/color_blue.css">');
    }
    
    if(typeof json.bannerImage !== 'undefined'){
        $('#banner-image').attr('src',json.bannerImage);
    }else{
        alert('no banner image assigned');
        $('#banner-image').attr('src',json.bannerImage);
    }
    
    if(typeof json.bannerText !== 'undefined'){
        $('#banner-text').html(json.bannerText);
    }else{
        $('#banner-text').html('');
    }
    
    if(typeof json.navTopImage !== 'undefined'){
        $('#nav-top-image').attr('src',json.navTopImage);
    }else{
        alert('no banner image assigned');
        $('#nav-top-image').attr('src',json.navTopImage);
    }
    
    for(var i=0; i<json.nav.length; i++){
        var navButton = $('<div class="nav-text-div" onclick="location.href = \''+json.nav[i].link+'\';">'+json.nav[i].linkName+'</div>');
        if(json.nav[i].activated === 1)
            navButton.addClass('nav-text-div-activated');
        $('#nav-div').append(navButton);
    }
    
    for(var i=0; i<json.content.length; i++){
        $('#contents-div').append(getContent(json.content[i]));
    }
    
    if(typeof json.footer !== 'undefined'){
        $('#footer').append('<br>'+json.footer);
    }else{
        $('#footer').append('no footer assigned');
    }
    
    if(typeof json.inEdit !== 'undefined'){
        if(json.inEdit===true)
            $('body').append($('<script src="js/editView.js"></script>'));
    }
	
	if(typeof json.font !== 'undefined'){
        $('head').append('<link rel="stylesheet" href="'+json.font+'">');
    }else{
        alert('no banner image assigned');
        $('head').append('<link rel="stylesheet" href="css/font01.css">');
    }
}

function getContent(content){
    switch(content.type){
		case 'heading':
			return headingContent(content)
        case 'text':
            return textContent(content);
		case 'list':
			return listContent(content);
        case 'image':
            return imageContent(content);
        case 'video':
            return videoContent(content);
        case 'slideShow':
            return slideShowContent(content);
        default:
            alert('content type incorrect');
    }
}

function headingContent(content){
	var textDiv = $('<div class="content-div content-heading-div"></div>');
	var text = content.content;
	var textElem = $('<h1 id="'+content.id+'" class="content content-heading">' + text + '</h1>');
	textDiv.append(textElem);
	return textDiv;
}

function textContent(content){
    var textDiv = $('<div class="content-div content-text-div"></div>');
    var text = content.content;
    for(var i=content.link.length-1; i>=0; i--){
        text = text.slice(0,content.link[i][0]) + 
                '<a href="'+content.link[i][2]+'" class="paragraph-link"  target="_blank">' + 
                text.slice(content.link[i][0],content.link[i][1]) + '</a>' + 
                text.slice(content.link[i][1]);
    }
    var textElem = $('<p id="'+content.id+'" class="content content-text">' + text + '</p>');
    textDiv.append(textElem);
    return textDiv;
}

function listContent(content){
	var listDiv = $('<div class="content-div content-list-div"></div>');
	var listUl = $('<ul id="'+content.id+'" class="content content-list" ></ul>');
	for(var i=0; i<content.list.length; i++){
		listUl.append($('<li>'+content.list[i]+'</li>'));
	}
	listDiv.append(listUl);
	return listDiv;
}

function imageContent(content){
    var imageDiv = $('<div class="content-div content-image-div"></div>');
	var align;
	if(content.floating === 'left')
		align = 'left';
	else if(content.floating === 'right')
		align = 'right';
	if (!content.content.trim()) {
		content.content = 'resources/default_picture.png'
	}
    var imageElem = $('<img id="'+content.id+'" class="content content-image" align="'+align+'" src="'+content.content+'"></img>');
    var caption = $('<p>'+content.caption+'</p>');
	imageDiv.append(imageElem);
	imageDiv.append(caption);
    return imageDiv;
}

function videoContent(content){
    var videoDiv = $('<div class="content-div content-video-div"></div>');
    var videoElem = $('<video id="'+content.id+'" class="content content-video" width="'+content.width+'" height="'+content.height+'" controls>'+
            '<source src="'+content.content+'" type="video/mp4"></source>'+
            '</video>');
	var caption = $('<p>'+content.caption+'</p>');
    videoDiv.append(videoElem);
	videoDiv.append(caption);
    return videoDiv;
}

function slideShowContent(content){
    var showDiv = $('<div class="content-div content-iframe-div"></div>');
    var showElem = $('<iframe width="100%" height="300px" id="'+content.id+'" class="content content-iframe" src="'+content.content+'" frameborder="0" style="background-color:grey;" ></iframe>');
    showDiv.append(showElem);
    return showDiv;
}


