app.service("indexService",function ($http) {

    //获取 后台返回的浏览器 当前 登入用户的 用户名
    this.getName=function () {

        return $http.get('../login/name.do');
    }
});