app.controller('searchController',function($scope,searchService){
    //搜索

    $scope.search=function(){
        alert(JSON.stringify($scope.searchMap));
        searchService.search( $scope.searchMap ).success(
            function(response){
                $scope.resultMap=response;//搜索返回的结果
                alert(JSON.stringify(response.rows[0]));
            }
        );
    }
});