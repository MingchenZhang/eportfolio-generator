slides = new Array();
slidesIndex = 0;
autoPlayIntervalStarted = false;

document.getElementById("slideshow-title").innerHTML = json.title;


slides = json.slides;

$('#slideshow-prevButton').click(prevButtonHandler);
$('#slideshow-nextButton').click(nextButtonHandler);
$('#slideshow-playButton').click(playButtonHandler);

refreshSlide(slidesIndex,1);

$(window).resize(function(){
	resizeSlide();
});

$(window).keyup(keyBoardHandler);

$(window).load(function(){
	resizeSlide();
});


function resizeSlide(){
	var bodyHeight = $('#slideshow-page').height();
	var windowHeight = $(window).height();
	var oldImageHeight = Number($('#slideshow-image').attr('height'));
	var changes = (windowHeight-bodyHeight);
	$('#slideshow-image').attr('height',oldImageHeight+changes);
}

function prevButtonHandler(){
	if(slidesIndex>0){
		slidesIndex--;
		refreshSlide(slidesIndex,-1);
	}else{
		refuseToChange(-1);
		pauseHandler();
	}
}

function nextButtonHandler(goBackToFirst){
	if(slidesIndex<slides.length-1){
		slidesIndex++;
		refreshSlide(slidesIndex,1);
	}else{
		if(goBackToFirst===true) {
			slidesIndex=0;
			refreshSlide(slidesIndex,-1);
		}else {
			refuseToChange(1);
			pauseHandler();
		}
	}
}

function playButtonHandler(){
	if(autoPlayIntervalStarted){
		pauseHandler()
	}else{
		autoPlayInterval = setInterval(function(){
			nextButtonHandler(true);
		},2000);
		autoPlayIntervalStarted = true;
		$('#slideshow-playButton').addClass('greenButton');
		document.getElementById('slideshow-playButton').innerHTML = '<img src="img/glyphicons-175-pause.png" class="button-image"></img>';
		//$('#slideshow-playButton').empty();
		//$('#slideshow-playButton').append($('<img src="img/glyphicons-175-pause.png"></img>'));
	}
}

function pauseHandler(){
	clearInterval(autoPlayInterval);
	autoPlayIntervalStarted = false;
	$('#slideshow-playButton').removeClass('greenButton');
	document.getElementById('slideshow-playButton').innerHTML = '<img src="img/glyphicons-174-play.png" class="button-image"></img>';
}

function refreshSlide(index,direction) {
	var dir;
	if(direction>0)dir = '-=';
	else dir = '+='
	if(slides[index].slides_caption != ''){
		document.getElementById("slideshow-caption").innerHTML = slides[index].slides_caption;
	}else{
		document.getElementById("slideshow-caption").innerHTML = ' ';
	}
		
	$('#slideshow-image').animate({left:dir+'50',opacity:'0.0'},100,function(){(function(index,direction){return displayNewSlide(index,direction)}(index,direction))});
}

function displayNewSlide(index,direction) {
	var dir;
	var dir2;
	if(direction>0){dir = '-=';dir2 = '+='}
	else {dir = '+=';dir2 = '-='}
	$('#slideshow-image').animate({left:dir2+'100'},1);
	var newImagePath = 'img/'+slides[index].image_file_name;
	$('#slideshow-image').attr("src",newImagePath);
	$('#slideshow-image').animate({left:dir+'50',opacity:'1.0'},100);
}

function refuseToChange(direction){
	var dir;
	var dir2;
	if(direction>0){dir = '-=';dir2 = '+='}
	else {dir = '+=';dir2 = '-='}
	$('#slideshow-image').animate({left:dir+'25',opacity:'1.0'},50);
	$('#slideshow-image').animate({left:dir2+'25',opacity:'1.0'},50);
}

function keyBoardHandler(event){
	if(event.keyCode == 37){//left Button
		prevButtonHandler();
	}
	else if(event.keyCode == 39){//right Button
		nextButtonHandler();
	}
	else if(event.keyCode == 32){//space Button
		playButtonHandler();
	}
}

//(function(index,direction){return displayNewSlide(index,direction)}(index,direction))
//(function(index,direction){return displayNewSlide(index,direction)}(index,direction))