<!--
 * @Description:
 * @Author:
 * @Date: 2024-03-08 20:38:49
-->
<template>
    <div class="index">
        <div class="welcome-message">
            <span class="welcome-text">您好：很高兴遇见你, {{ user.userName }}讲师, 欢迎您登录在线考试系统（教师端）</span>
        </div>
        <div class="image-container">
            <img
                :src="require('@/assets/bg11.jpg')"
                class="welcome-image"
                alt="欢迎图片"
            />
        </div>
    </div>
</template>

<script>
export default {
    data() {
        return {
            user: {
                //用户信息
                userName: null,
                userId: null,
            },
        };
    },
    created() {
        this.getUserInfo();
    },
    methods: {
        getUserInfo() {
            //获取用户信息
            let userName = this.$cookies.get("cname");
            let userId = this.$cookies.get("cid");
            this.user.userName = userName;
            this.user.userId = userId;
        },
    },
};
</script>


<style lang="less" scoped>




.index {
    padding: 40px 20px;
    text-align: center;
    min-height: calc(100vh - 200px);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    
    .welcome-message {
        margin-bottom: 40px;
        max-width: 800px;
        
        .welcome-text {
            color: var(--text-primary);
            font-size: 42px;
            font-weight: 700;
            line-height: 1.5;
            display: block;
            text-align: center;
            background: linear-gradient(135deg, var(--button-primary), var(--button-secondary));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            letter-spacing: 1px;
        }
    }
    
    .image-container {
        margin-top: 30px;
        text-align: center;
        
        .welcome-image {
            max-width: 70vw;
            max-height: 400px;
            width: auto;
            height: auto;
            border-radius: 12px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            
            &:hover {
                transform: translateY(-5px);
                box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
            }
        }
    }
    
    .hello {
        font-size: 28px;
        color: var(--text-primary);
    }
    .msg {
        .title {
            font-size: 16px;
            color: var(--text-primary);
            margin-top: 20px;
            margin-left: 10px;
        }
        ul {
            display: flex;
            flex-direction: column;
            width: 200px;
            overflow: hidden;
        }
        li {
            margin-top: 10px;
            font-size: 14px;
            color: var(--text-secondary);
            cursor: pointer;
            display: inline-block;
            
            &:hover {
                color: var(--button-primary);
            }
        }
    }
}
</style>

