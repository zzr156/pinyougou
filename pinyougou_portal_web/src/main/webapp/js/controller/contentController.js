app.controller("contentController",function($scope,  $location,contentService){


    $scope.contentList=[];//广告集合
    $scope.findByCategoryId=function(categoryId){
    	// alert("触发");
        contentService.findByCategoryId(categoryId).success(
            function(response){
                $scope.contentList[categoryId]=response;
            }
        );
    }


    //首页跳转  搜索返回列表 页
    $scope.search=function () {
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
});
