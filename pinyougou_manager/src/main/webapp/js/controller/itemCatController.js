//控制层
app.controller('itemCatController', function ($scope, $controller, itemCatService,typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        itemCatService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        itemCatService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        itemCatService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        itemCatService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        itemCatService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数

                if (response.rows[0].parentId == 0) {//当父id是0时，代表顶级列表,截取缓存下来
                    $scope.pEntitylist = response.rows;
                }
            }
        );
    };

//商品分类功能
    //初始化数据
    $scope.parentList = [{pid: 0, pname: "顶级分类列表", entitys: $scope.pEntitylist}];
    $scope.node = 3;
    $scope.grade=1;//用以作角标，

    //步进 查询下级( 根据pid查询列表
    $scope.forward = function (entity) {
        //修改查询条件
        $scope.searchEntity = {parentId: entity.id};
        $scope.reloadList();
        //此时的list，就是 最顶级列表,entity就是二级的父模型（当时的列表就是二级列表
        //步进，此时的父模型 增加一个 进度，（维持
        $scope.updateParent(entity);
    };

    //跃退 面包屑查询（处理 父模型更新，取出期望的最后 元素的列表)
    $scope.back = function (hnode) {
        //更新节点
        $scope.node = hnode;

//角标与级别 差1
       $scope.searchEntity = {parentId:$scope.parentList[hnode-1].pid};
        $scope.reloadList();

   // $scope.list = $scope.parentList[hnode].entitys;

        //更新 p模型:点击——node变化，期望实际关系变化，往回拉
        $scope.updateParent(null);

    };

    //更新 父模型(步进与跃退)
    $scope.updateParent = function (entity) {
        if(entity){

            if ($scope.parentList.length < $scope.node) {//趋势
                $scope.parentList.push({pid: entity.id, pname: entity.name, entitys: $scope.list});

            $scope.grade=$scope.parentList.length;

            }}
        if(entity==null){
            if($scope.node==2){ $scope.parentList.splice($scope.node, 1);$scope.node=3;}
            if($scope.node==1){ $scope.parentList.splice($scope.node, 2);$scope.node=3;}

            $scope.grade=$scope.parentList.length;
        }
    };

//商品分类功能

    //获取 模板 选项
    $scope.typelist={data:[]};
    $scope.getTypeOption=function(){
        typeTemplateService.findAll().success(
            function(response){

                for (var i = 0; i < response.length; i++) {
                 var tid=  response[i].id;
                 var ttext=response[i].name;

                  $scope.typelist.data.push({id:tid,text:ttext});
                }
            }
        );
    }  ;

    //保存
    $scope.saveEntity={};//编辑窗口 封装类
    $scope.save = function () {
        var serviceObject;//服务层对象

        //封装 编辑框 数据pid,tid,原有name
        $scope.saveEntity.parentId=$scope.parentList[$scope.grade-1].pid;
        $scope.saveEntity.typeId=$scope.type.id;

        if ($scope.saveEntity.id != null) {//如果有ID

            serviceObject = itemCatService.update($scope.saveEntity); //修改

        } else {

            serviceObject = itemCatService.add($scope.saveEntity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }
    
    $scope.getTypeIdAndName=function (entity) {//
        //回显 name
        $scope.saveEntity.name=entity.name;

        //回显所有 模板选项
        $scope.getTypeOption();

        //回显当前的 模板
       // $scope.typelist={data:[{id:entity.typeId,text:ttext}]};
        //根据id查询模板选项
       /* typeTemplateService.findOne(entity.typeId).success(
            function (response) {
           // $scope.typelist={data:[{id:response.id,text:response.name}]};
        })*/

    }

})
;
