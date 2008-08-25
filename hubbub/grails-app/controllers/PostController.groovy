class PostController {

    def index = { }

    def list = {
        def userDbId = session.user.id
        // detached object...
        def user = User.get(userDbId)

        def idsToInclude = user.following.collect { u -> u.id }
        idsToInclude.add(user.id)
        println idsToInclude.dump()

//        def crit = Post.createCriteria()
//
//		def allPosts = crit.list {
//            maxResults(20)
//            order('created', 'desc')
//            user {
//                'in'('id', idsToInclude.asList())
//            }
//
//        }

//        def query = "from Post as p where p.user.id in ( " + idsToInclude.join(",") + " ) order by p.created desc)"
//        log.debug "Recent posts query is ${query}"
//        def allPosts = Post.findAll(query)

// this is broken for named params...
        def allPosts = Post.findAll("from Post as p where p.user.id in ( " + idsToInclude.join(",") + " ) order by p.created desc",
                [ max: 10, offset: 0])
        println "Post count is ${allPosts?.size()}"


        [ followingCount : user.following.size(),
          followersCount : user.followers.size(),
              postsCount : user.posts.size(),
               following : user.following,
                allPosts : allPosts ]
    }

    def show = {

        if (params.id) {
            def user = User.findByUserId(params.id)
            if (user) {
                [ viewUser : user ]

            }
        }

    }

    def add = {

        def content = params.postContent
        if (content) {
            def user = User.get(session.user.id)
            if (user) {
                user.addToPosts(new Post(content: content))
                flash.message = "Added new post"
            }
        }
        redirect(action: 'list')

    }

    def byTag = {

        def tag = params.id
        def user = User.findByUserId('glen')
        def postCriteria = Post.createCriteria()

        def entries = postCriteria.list {
            and {
                eq('user', user)
                between('created', new Date()-1, new Date())
                tags {
                    eq('name', tag)
                }
            }
            cacheable(true)
            maxResults(10)
            order("created", "desc")
        }
        println "Found ${entries?.size()} users with tag ${tag}"
        println entries?.dump()


    }
}

