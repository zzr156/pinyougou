var app=angular.module('pinyougou',[]);


//使用过滤器  来全局  投注 信任策略
app.filter('trustHtml',['$sce',function ($sce) {
   return function (data) {
       return $sce.trustAsHtml(data);
   }
}]);