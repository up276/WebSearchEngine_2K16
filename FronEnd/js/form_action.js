$( document ).ready(function() {
    var url      = window.location.href;     // Returns full URL
    var getUrlParameter = function getUrlParameter(sParam) {
        var sPageURL = decodeURIComponent(window.location.search.substring(1)),
            sURLVariables = sPageURL.split('&'),
            sParameterName,
            i;

        for (i = 0; i < sURLVariables.length; i++) {
            sParameterName = sURLVariables[i].split('=');

            if (sParameterName[0] === sParam) {
                return sParameterName[1] === undefined ? true : sParameterName[1];
            }
        }

    };


    $("#searchBar").val(decodeURI(getUrlParameter("query")).replace(/\+/g, ' '));
    $("#ranker").val(getUrlParameter("ranker"));
    $("#num").val(getUrlParameter("num"));
    $("#format").val(getUrlParameter("format"));

    $( "#submitSearch" ).click(function() {
        $("#searchKey").val($("#searchBar").val());
        $( "#searchForm" ).submit();
    });


});





