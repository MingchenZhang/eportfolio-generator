/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var justSelectLike = false;
$('.content-div').hover(selectColoringHandler, selectDecoloringHandler);
$('.content-div').click(selectHandler);

$('.paragraph-link').attr('href', '');
$('.paragraph-link').click(hyperlinkSelector);

function selectColoringHandler(event){
    var coverDiv;
    if($(event.currentTarget).hasClass('content-text-div'))
        coverDiv = $('<div class="cover-div cover-div-under"></div>');
    else
        coverDiv = $('<div class="cover-div"></div>');
    //console.log(event.currentTarget);
    $(event.currentTarget).append(coverDiv);
}
function selectDecoloringHandler(event){
    $(event.currentTarget).find('.cover-div').remove();
}

function selectHandler(event){
	if(justSelectLike){
		justSelectLike = false;
		return;
	}
	
    console.log($(event.currentTarget).find('.content').attr('id'));
    
    var contentElement = $(event.currentTarget).find('.content');
    if(	contentElement.hasClass('content-text') && 
		!window.getSelection().isCollapsed && // start != end
		$(window.getSelection().anchorNode.parentElement).hasClass('content-text') && 
		$(window.getSelection().focusNode.parentElement).hasClass('content-text')){
        console.log('current focus: '+$(window.getSelection().anchorNode.parentElement).attr('id'));
        var textSelectStart = window.getSelection().anchorOffset;
        var textSelectEnd = window.getSelection().focusOffset;
        //swap if reversed
        if(textSelectStart>textSelectEnd){
            var temp = textSelectStart;
            textSelectStart = textSelectEnd;
            textSelectEnd = temp;
        }
		
		//get relative position from element
		var nodesInElement = window.getSelection().anchorNode.parentElement.childNodes;
		var characterCounter = 0;
		for(var i=0; i<nodesInElement.length; i++){
			if(window.getSelection().anchorNode === nodesInElement[i]) break;
			if(nodesInElement[i].nodeType === 3){//text node
				characterCounter+= nodesInElement[i].wholeText.length;
			}else if(nodesInElement[i].nodeType === 1) {//hyperlink element node{
				characterCounter+= nodesInElement[i].firstChild.wholeText.length;
			}
		}
		textSelectStart += characterCounter;
		textSelectEnd += characterCounter;
        
        console.log('selection start at: '+textSelectStart+'. end at: '+textSelectEnd);
        if (typeof javaInterface !== 'undefined') {//typeof javaInterface !== 'undefined'
    		window.javaInterface.selectText(contentElement.attr('id'), textSelectStart+'', textSelectEnd+'');
		}
    }else{
        if (typeof javaInterface !== 'undefined') {
    		window.javaInterface.selectItem(contentElement.attr('id'));
		}
    }
}

function getTextSelection(){
    var selection = window.getSelection();
    if(!selection.isCollapsed && $(selection.anchorNode).hasClass('content-text') && $(selection.focusNode.parentElement).hasClass('content-text')){
        var textSelectStart = selection.anchorOffset;
        var textSelectEnd = selection.focusOffset;
        //swap is reversed
        if(textSelectStart>textSelectEnd){
            var temp = textSelectStart;
            textSelectStart = textSelectEnd;
            textSelectEnd = temp;
        }
        return $(selection.focusNode).attr('id')+' '+textSelectStart+' '+textSelectEnd;
    }
    return null;
}

function hyperlinkSelector(event){
	//get relative position from element
	var nodesInElement = event.currentTarget.parentElement.childNodes;
	var characterCounter = 0;
	for(var i=0; i<nodesInElement.length; i++){
		if(event.currentTarget === nodesInElement[i]) break;
		if(nodesInElement[i].nodeType === 3){//text node
			characterCounter+= nodesInElement[i].wholeText.length;
		}else if(nodesInElement[i].nodeType === 1) {//hyperlink element node{
			characterCounter+= nodesInElement[i].firstChild.wholeText.length;
		}
	}
	console.log('hyperlink selected, starts at: '+characterCounter);
	if (typeof javaInterface !== 'undefined') {//typeof javaInterface !== 'undefined'
    	window.javaInterface.selectText($(event.currentTarget.parentElement).attr('id'), characterCounter+'', '-1');
	}
	justSelectLike = true;
}