using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Internal;
using Microsoft.EntityFrameworkCore.Metadata.Internal;

namespace azure_github_ci_cd.Model
{
    public class AzureDbContext : DbContext
    {
        public DbSet<User> Users { get; set; }

        public AzureDbContext(DbContextOptions options) : base(options)
        {
        }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            foreach (var entity in modelBuilder.Model.GetEntityTypes())
            {
                entity.Relational().TableName = entity.DisplayName();
            }
        }
    }

    public class User
    {
        public int Id { get; set; }
        public string Name { get; set; }
    }
}
