var gulp = require('gulp');
var replace = require('gulp-replace');

gulp.task('hoge',function() {
  gulp.src('index.html')
    .pipe(replace('git_commit_hash_placeholder', process.env.GIT_COMMIT_HASH))
    .pipe(gulp.dest('dist/'));
});
