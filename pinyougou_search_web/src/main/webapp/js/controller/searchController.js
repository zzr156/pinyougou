app.controller('searchController',function($scope,searchService){
    //搜索

    $scope.search=function(){
        // alert(JSON.stringify($scope.searchMap));
        searchService.search( $scope.searchMap ).success(
            function(response){
                $scope.resultMap=response;//搜索返回的结果
                // alert(JSON.stringify(response.rows[0]));

                //分页构件 分页栏
                $scope.pageLabel=[];
                $scope.buildPageLabel();
            }
        );
    }

    //   定义 筛选模型
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40};
    //维持 模型
    $scope.addSearchItem=function (key,value) {
        // 初始。牵引因子：key-value  点击所选的统属上级，以及自身实例质点
        if (key == 'category' || key == 'brand'||key=='price') {

            $scope.searchMap[key]=value;
        }else {

           /* if ($scope.searchMap[key].length == 0) {
                //此处value是  {optionName:[options],}结构对象
                $scope.searchMap[key] = value;//  该keyName 的option无值，即无质
            }else {
                //如果keyName 的option 有值，需要判断 是否重复
                for(var i=0;i<$Scope.searchMap[key].length;i++) {
                    if($scope.searchMap[key][i]==value){}
                    else {}
                }
            }*/
           $scope.searchMap.spec[key]=value;
        }
        $scope.search();//执行搜索
    }

    //移除复合搜索条件
    $scope.removeSearchItem=function(key){

        if(key=="category" ||  key=="brand"||key=='price'){//如果是分类或品牌
            $scope.searchMap[key]="";
        }else{//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }
        $scope.search();//执行搜索
    }


    // 分页维持机制
    $scope.buildPageLabel=function () {
        //获取分页栏 索引
        for(var i =1; i<=$scope.resultMap.totalPages;i++){
            $scope.pageLabel.push(i);
        }
    }

});