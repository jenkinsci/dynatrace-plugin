/**
 * Created by krzysztof.necel on 2016-04-21.
 */
function expandableDivHeaderOnClick(item) {
    var theDiv = item.next();
    var icon = item.childNodes[0];
    var iconHidden = item.childNodes[1];

    var temp = icon.src;
    icon.src = iconHidden.src;
    iconHidden.src = temp;

    if(theDiv.style.display=='none') {
        theDiv.appear();
    } else {
        theDiv.hide();
    }
}