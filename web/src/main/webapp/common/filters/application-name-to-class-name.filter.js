/**
 * @namespace
 */
(function() {
	'use strict';
	/**
	 * 将字符串中的'.', '^', ':'用'_'替换
	 * @ko 문자열에 포함된 '.', '^', ':' 문자를 '_' 문자로 치환한다.
	 * @group Filter
	 * @name pinpointApp#applicationNameToClassName
	 * @method pinpointApp#applicationNameToCalssName
	 * @param {String} inputString
	 * @return {String} converted string 
	 * @example 
	 * ```
	 * expect( applicationNameToClassName("com.test.domain") ).toEqual( "com_test_domain" );
	 * ```
	 */
	angular.module('pinpointApp').filter('applicationNameToClassName', function () {
	    return function (input) {
	    	return input.replace( /[\.\^:]/gi, '_');
	    };
	});
})();