<!--
 * @Description:
 * @Author:
 * @Date: 2024-03-08 20:38:49
-->
<template>
  <div id="app">
    <router-view/>
  </div>
</template>

<script>
export default {
  name: 'App',
  created() {
    // 提前初始化主题，确保body有正确的主题类
    const savedTheme = localStorage.getItem('theme-mode');
    const isDarkMode = savedTheme === 'dark';
    if (isDarkMode) {
      document.body.classList.add('dark-theme');
      document.body.classList.remove('light-theme');
    } else {
      document.body.classList.add('light-theme');
      document.body.classList.remove('dark-theme');
    }
    
    // 根据用户角色添加相应的CSS类，防止主题污染
    var role = this.$cookies.get("role") || sessionStorage.getItem('role');
    if (role === '1') {
      // 教师端
      document.body.classList.add('teacher-page');
      document.body.classList.remove('admin-page', 'student-page');
    } else if (role === '0') {
      // 管理员端
      document.body.classList.add('admin-page');
      document.body.classList.remove('teacher-page', 'student-page');
    } else if (role === '2') {
      // 学生端
      document.body.classList.add('student-page');
      document.body.classList.remove('teacher-page', 'admin-page');
    }
    
    var token = this.$cookies.get("rb_token");
    if(token == null || token == "" || role == null || role == null) {
      this.$router.push({path: '/' }) //跳转到首页
    }
    
  }
}
</script>

<style>

.icon-r-yes {
  display: none;
}

  ul {
  list-style: none;
}
a {
  text-decoration: none;
}
* {
  margin: 0;
  padding: 0;
}
#app {
  font-family: "Microsoft YaHei", "Helvetica", "Tahoma", "Geneva", "Arial", sans-serif;
  background-color: #eee;
}
</style>
