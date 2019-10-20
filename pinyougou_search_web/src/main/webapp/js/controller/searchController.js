app.controller('searchController', function ($scope, $location,searchService) {
    //搜索

    $scope.search = function () {
        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;
        // alert(JSON.stringify($scope.searchMap));
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;//搜索返回的结果
                buildPageLabel();//调用
            }
        );
    }

    //   定义 筛选模型
    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': 1,
        'pageSize': 40,
        'sort':'',
        'sortField':''
    };
    //维持 模型
    $scope.addSearchItem = function (key, value) {
        // 初始。牵引因子：key-value  点击所选的统属上级，以及自身实例质点
        if (key == 'category' || key == 'brand' || key == 'price') {

            $scope.searchMap[key] = value;
        } else {

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
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();//执行搜索
    }

    //移除复合搜索条件
    $scope.removeSearchItem = function (key) {

        if (key == "category" || key == "brand" || key == 'price') {//如果是分类或品牌
            $scope.searchMap[key] = "";
        } else {//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }
        $scope.search();//执行搜索
    }

//构建分页栏
    buildPageLabel=function(){
        //构建分页栏
        $scope.pageLabel=[];
        var firstPage=1;//开始页码
        var lastPage=$scope.resultMap.totalPages;//截止页码
        $scope.firstDot=true;//前面有点
        $scope.lastDot=true;//后边有点
        if($scope.resultMap.totalPages>5){  //如果页码数量大于5
            if($scope.searchMap.pageNo<=3){//如果当前页码小于等于3 ，显示前5页
                lastPage=5;
                $scope.firstDot=false;//前面没点
            }else if( $scope.searchMap.pageNo>= $scope.resultMap.totalPages-2 ){//显示后5页
                firstPage=$scope.resultMap.totalPages-4;
                $scope.lastDot=false;//后边没点
            }else{  //显示以当前页为中心的5页
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;
            }
        }else{
            $scope.firstDot=false;//前面无点
            $scope.lastDot=false;//后边无点
        }
        //构建页码
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }
    //根据页码查询
    $scope.queryByPage=function(pageNo){
        //页码验证
        if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();
    }

    //判断当前页为第一页
    $scope.isTopPage=function(){
        if($scope.searchMap.pageNo==1){
            return true;
        }else{
            return false;
        }
    }

    //判断当前页是否未最后一页
    $scope.isEndPage=function(){
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    }

    
    //排序
    $scope.sortSearch=function (sortField,sort) {
        //干涉  ：对指 参数searchMap
        $scope.searchMap.sort=sort;
        $scope.searchMap.sortField=sortField;
        $scope.search();
    }

    // 品牌 ，直接某品牌的产品进行 商品查询,隐藏 品牌回显列表
    $scope.keywordsIsBrand=function () {
        //遍历回显 的 品牌列表，判断是否 关键字 存在 品牌的子串
        var brandlist=$scope.resultMap.brandList;
        for(var i = 0 ; i<brandlist.length;i++){
            //int indexOf​(String str) 返回指定子字符串第一次出现的字符串内的索引。
            if($scope.searchMap.keywords.indexOf(brandlist[i].text)>=0){//如果 有返回，不为-1,则 含有 子串
                return true;
            }

        }
        return false;
    }

    //首页跳转
    $scope.loadkeywords=function () {
        alert("触发")
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }

    // 分页维持机制
    // $scope.pageObject={'pageNo':1,'pageLabel':[],'pageFirst':1,'pageLast':5};




});

/*
*     /*视界 变化 控制：
    *   控制  pageFirst 和 pageLast实现
    * 高亮 变化:  实现高亮，点击无效不变化
    *
// 分页栏视界 w维持机制
$scope.pageNo=1;//初始化为第一页， 在 search调用，当页面为1时，触发初始化模式分页栏，否则触发 变动模式分页栏

$scope.updatePage = function (pageNo) {
        $scope.pageNo=pageNo;
        alert($scope.pageNo);
        if ($scope.pageNo >3) {
            //如果 期望的当前页  大于  视界半径 隔膜下限， 视界区域 变化  （由2点击3时，仍然不变化
            //查询后台:
            $scope.search();
            // 控制:  视界上限 为 -2，下限为+2
            $scope.pageFirst = $scope.pageNo - 2;
            $scope.pageLast = $scope.pageNo + 2;
            alert("pageNo>3:"+$scope.pageFirst+"与"+$scope.pageLast);
            $scope.buildPageLabel();


        } else {//如果 期望当前页 在 视界隔膜下限半径内，当前页高亮会变化，但是当临界到 1 时，点击无效
            if ($scope.pageNo > 1) {//高亮 变化，视界不变化
                // 高亮变化（查询)  控制：1-5  (在 初次 分页栏时 初始化)
                //查询后台:
                $scope.search();
                alert("节点")
                alert($scope.pageFirst+"与"+$scope.pageLast);
                $scope.buildPageLabel();
            }//=1  不做动作
        }

        if ($scope.pageNo <  $scope.resultMap.totalPages - 2) {
            //查询后台
            $scope.search();
            //如果 期望 的当前页  小于 视界半径 隔膜上限，  视界区域变化 （如 最大100页，99，98 页时，点击视界不变化
            $scope.pageFirst = $scope.pageNo - 2;
            $scope.pageLas = $scope.pageNo + 2;
            alert($scope.pageFirst+"与"+$scope.pageLast);

            $scope.buildPageLabel();



        } else {//如果 期望 的当前页 在 视界隔膜上限半径 内，高亮可变 ，临界到100点击无效
            if ($scope.pageNo < $scope.resultMap.totalPages) {//点击照常有效，视界不变，高亮当前页变化
                //查询后台, 视界 为  末尾视界
                $scope.search();
                $scope.pageFirst = $scope.resultMap.totalPages - 4;
                $scope.pageLast = $scope.resultMap.totalPages;
                alert($scope.pageFirst+"与"+$scope.pageLast);

                $scope.buildPageLabel();
            }//=最大值，不做动作
        }
    }//注意 先有查询后台 ，后有回流 数据totalPages

    * */