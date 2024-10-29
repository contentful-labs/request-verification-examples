using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddControllers();

var app = builder.Build();

// Uncomment the line below if you want to use HTTPS redirection and have configured the HTTPS port properly
// app.UseHttpsRedirection();

// Enable routing
app.UseRouting();

// Authorization middleware (can be left as-is if you don't need any authentication for the webhook)
app.UseAuthorization();

// Map controller endpoints
app.MapControllers();

// Run the application
app.Run();
